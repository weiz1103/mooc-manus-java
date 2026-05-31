package com.imooc.manus.springai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.springai.domain.port.SandboxPort;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Shell 工具服务，提供命令执行、输出读取、进程管理等能力。
 * <p>
 * </p>
 *
 * <p><b>扩展点：</b> 覆盖工具方法可添加安全审计、资源限制等横切关注点。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-26 01:30:36
 */
public class ShellToolService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SandboxPort sandbox;

    public ShellToolService(SandboxPort sandbox) {
        this.sandbox = sandbox;
    }

    @Tool(name = "shell_execute", description = "在指定 Shell 会话中执行命令。可用于运行代码、安装依赖包或文件管理。")
    public String shellExecute(
            @ToolParam(description = "Shell 会话 id") String sessionId,
            @ToolParam(description = "命令执行目录（绝对路径）", required = false) String execDir,
            @ToolParam(description = "要执行的 Shell 命令") String command
    ) {
        return serialize(sandbox.execCommand(sessionId, execDir, command));
    }

    @Tool(name = "shell_read_output", description = "查看指定 Shell 会话的输出内容。用于检查命令执行结果或监控输出。")
    public String shellReadOutput(
            @ToolParam(description = "Shell 会话 id") String sessionId
    ) {
        return serialize(sandbox.readShellOutput(sessionId, false));
    }

    @Tool(name = "shell_wait_process", description = "等待指定 Shell 会话中正在运行的进程返回。在运行耗时较长的命令后使用。")
    public String shellWaitProcess(
            @ToolParam(description = "Shell 会话 id") String sessionId,
            @ToolParam(description = "等待超时秒数（可选）", required = false) Integer seconds
    ) {
        return serialize(sandbox.waitProcess(sessionId, seconds));
    }

    @Tool(name = "shell_write_input", description = "向指定 Shell 会话中正在运行的进程写入输入。用于响应交互式命令提示符。")
    public String shellWriteInput(
            @ToolParam(description = "Shell 会话 id") String sessionId,
            @ToolParam(description = "要写入的文本内容") String inputText,
            @ToolParam(description = "是否在输入后按 Enter 键") Boolean pressEnter
    ) {
        return serialize(sandbox.writeShellInput(sessionId, inputText, Boolean.TRUE.equals(pressEnter)));
    }

    @Tool(name = "shell_kill_process", description = "在指定 Shell 会话中终止正在运行的进程。用于停止长时间运行的进程或处理卡住的命令。")
    public String shellKillProcess(
            @ToolParam(description = "Shell 会话 id") String sessionId
    ) {
        return serialize(sandbox.killProcess(sessionId));
    }

    private String serialize(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"序列化失败\"}";
        }
    }
}

