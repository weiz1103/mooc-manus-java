package com.imooc.manus.sandbox.service;

import com.imooc.manus.common.dto.shell.ShellDto;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Shell 执行服务，对应 Python Sandbox 中的 shell 相关功能。
 *
 * <p>关键改进（相较上版）：
 * <ul>
 *   <li>使用 Java 21 虚拟线程读取输出（高并发低开销）</li>
 *   <li>ShellSession 作为内部领域对象封装状态</li>
 *   <li>超时时间可配置，强制 destroyForcibly 保证资源释放</li>
 *   <li>无内存泄漏：进程结束后输出仍可读取</li>
 * </ul>
 */
@Service
@Slf4j
public class ShellService {

    /** 每个执行会话的状态，对应 Python shell session */
    @Data
    @Builder
    private static class ShellSession {
        private String id;
        private Process process;
        private StringBuilder outputBuffer;
        private List<ShellDto.ConsoleRecord> records;
        private String currentDir;
        private volatile boolean completed;
    }

    private final ConcurrentHashMap<String, ShellSession> sessions = new ConcurrentHashMap<>();
    // Java 21 虚拟线程 Executor（轻量级，适合 IO 密集型读取）
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    /**
     * 执行 Shell 命令，对应 Python sandbox.run_command()
     */
    public ShellDto.ShellExecuteResult executeCommand(String sessionId, String execDir, String command) {
        log.info("[{}] Executing: {}", sessionId, command);

        // 1. 强制终止旧进程
        terminateSession(sessionId);

        // 2. 解析工作目录
        String workDir = (execDir != null && !execDir.isBlank()) ? execDir
                : System.getProperty("user.home");

        // 3. 构建 ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder(buildShellCommand(command));
        pb.directory(new File(workDir));
        pb.redirectErrorStream(true);  // 合并 stderr 到 stdout

        try {
            Process process = pb.start();

            StringBuilder outputBuffer = new StringBuilder();
            ShellDto.ConsoleRecord record = ShellDto.ConsoleRecord.builder()
                    .ps1("[" + sessionId + "]$ ")
                    .command(command)
                    .output("")
                    .build();
            List<ShellDto.ConsoleRecord> records = new ArrayList<>();
            records.add(record);

            ShellSession session = ShellSession.builder()
                    .id(sessionId)
                    .process(process)
                    .outputBuffer(outputBuffer)
                    .records(records)
                    .currentDir(workDir)
                    .completed(false)
                    .build();
            sessions.put(sessionId, session);

            // 4. 虚拟线程异步读取输出（不阻塞主线程）
            virtualThreadExecutor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String newLine = line + "\n";
                        outputBuffer.append(newLine);
                        record.setOutput(record.getOutput() + newLine);
                    }
                } catch (IOException e) {
                    log.warn("[{}] Output reader interrupted: {}", sessionId, e.getMessage());
                } finally {
                    session.setCompleted(true);
                }
            });

            // 5. 等待命令完成（30s 超时）
            boolean done = process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (done) {
                // 稍等输出读取完毕
                Thread.sleep(100);
                return ShellDto.ShellExecuteResult.builder()
                        .sessionId(sessionId)
                        .command(command)
                        .status(process.exitValue() == 0 ? "completed" : "failed")
                        .returnCode(process.exitValue())
                        .output(outputBuffer.toString())
                        .build();
            } else {
                // 超时，任务仍在运行
                return ShellDto.ShellExecuteResult.builder()
                        .sessionId(sessionId)
                        .command(command)
                        .status("running")
                        .output(outputBuffer.toString())
                        .build();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ShellDto.ShellExecuteResult.builder()
                    .sessionId(sessionId).command(command).status("failed")
                    .output("Interrupted: " + e.getMessage()).build();
        } catch (IOException e) {
            log.error("[{}] Failed to start process: {}", sessionId, e.getMessage());
            return ShellDto.ShellExecuteResult.builder()
                    .sessionId(sessionId).command(command).status("failed")
                    .output("Error: " + e.getMessage()).build();
        }
    }

    /**
     * 读取指定会话的 Shell 输出，对应 Python sandbox.read_output()
     */
    public ShellDto.ShellReadResult readOutput(String sessionId) {
        ShellSession session = sessions.get(sessionId);
        if (session == null) {
            return ShellDto.ShellReadResult.builder()
                    .sessionId(sessionId).output("").consoleRecords(new ArrayList<>()).build();
        }
        return ShellDto.ShellReadResult.builder()
                .sessionId(sessionId)
                .output(session.getOutputBuffer().toString())
                .consoleRecords(session.getRecords())
                .build();
    }

    /**
     * 强制终止会话进程
     */
    public void terminateSession(String sessionId) {
        ShellSession old = sessions.remove(sessionId);
        if (old != null && old.getProcess() != null && old.getProcess().isAlive()) {
            old.getProcess().destroyForcibly();
            log.info("[{}] Process terminated forcibly", sessionId);
        }
    }

    private List<String> buildShellCommand(String command) {
        if (WINDOWS) {
            return Arrays.asList("cmd.exe", "/d", "/s", "/c", command);
        }
        return Arrays.asList("/bin/bash", "-lc", command);
    }
}
