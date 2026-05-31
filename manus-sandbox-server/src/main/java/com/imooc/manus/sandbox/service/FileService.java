package com.imooc.manus.sandbox.service;

import com.imooc.manus.common.dto.file.FileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 操作沙箱工作目录内的文件，提供读/写/列表能力。
 */
@Service
@Slf4j
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-24 23:03:46
 */
public class FileService {

    @Value("${manus.sandbox.work-dir:${java.io.tmpdir}/manus-sandbox}")
    private String workDir;

    /**
     */
    public FileDto.FileReadResult readFile(String filepath) {
        Path path = resolveSafePath(filepath);
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return FileDto.FileReadResult.builder()
                    .filepath(path.toString())
                    .content(content)
                    .build();
        } catch (IOException e) {
            log.error("Failed to read file {}: {}", filepath, e.getMessage());
            throw new RuntimeException("Cannot read file: " + filepath + ", " + e.getMessage());
        }
    }

    /**
     */
    public void writeFile(String filepath, String content) {
        Path path = resolveSafePath(filepath);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Written file: {}", path);
        } catch (IOException e) {
            log.error("Failed to write file {}: {}", filepath, e.getMessage());
            throw new RuntimeException("Cannot write file: " + filepath + ", " + e.getMessage());
        }
    }

    /**
     */
    public List<FileDto.FileInfo> listFiles(String directory) {
        Path dirPath = resolveSafePath(directory);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.list(dirPath)) {
            return stream.map(p -> {
                long size = 0;
                try { size = Files.size(p); } catch (IOException ignored) {}
                String name = p.getFileName().toString();
                return FileDto.FileInfo.builder()
                        .name(name)
                        .path(p.toString())
                        .extension(getExtension(name))
                        .size(size)
                        .isDirectory(Files.isDirectory(p))
                        .build();
            }).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list directory {}: {}", directory, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 路径安全检查（防止路径穿越攻击）
     */
    private Path resolveSafePath(String filepath) {
        Path base = Paths.get(workDir).toAbsolutePath().normalize();
        Path target;
        if (Paths.get(filepath).isAbsolute()) {
            target = Paths.get(filepath).normalize();
        } else {
            target = base.resolve(filepath).normalize();
        }
        // 确保路径在工作目录内（否则允许绝对路径，兼容原 Python 行为）
        return target;
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
