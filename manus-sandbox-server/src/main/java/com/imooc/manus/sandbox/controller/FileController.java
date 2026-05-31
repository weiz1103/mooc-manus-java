package com.imooc.manus.sandbox.controller;

import com.imooc.manus.common.dto.ApiResponse;
import com.imooc.manus.common.dto.file.FileDto;
import com.imooc.manus.sandbox.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-29 22:36:37
 */
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
            @RequestParam(defaultValue = "/tmp/manus-sandbox") String directory) {
        return ApiResponse.success(fileService.listFiles(directory), "获取文件列表成功");
    }
}
