package io.github.weiz1103.agentflow.sandbox.controller;

import io.github.weiz1103.agentflow.common.dto.ApiResponse;
import io.github.weiz1103.agentflow.common.dto.file.FileDto;
import io.github.weiz1103.agentflow.sandbox.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/read")
    public ApiResponse<FileDto.FileReadResult> readFile(@RequestBody FileDto.FileReadRequest request) {
        return ApiResponse.success(fileService.readFile(request.getFilepath()), "读取文件成功");
    }

    @PostMapping("/write")
    public ApiResponse<Void> writeFile(@RequestBody FileDto.FileWriteRequest request) {
        fileService.writeFile(request.getFilepath(), request.getContent());
        return ApiResponse.success("写入文件成功");
    }

    @GetMapping("/list")
    public ApiResponse<List<FileDto.FileInfo>> listFiles(
            @RequestParam(defaultValue = "/tmp/AgentFlow-sandbox") String directory) {
        return ApiResponse.success(fileService.listFiles(directory), "获取文件列表成功");
    }
}


