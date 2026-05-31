package io.github.weiz1103.agentflow.sandbox.controller;

import io.github.weiz1103.agentflow.common.dto.ApiResponse;
import io.github.weiz1103.agentflow.common.dto.shell.ShellDto;
import io.github.weiz1103.agentflow.sandbox.service.ShellService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shell")
@RequiredArgsConstructor
public class ShellController {

    private final ShellService shellService;

    @PostMapping("/exec")
    public ApiResponse<ShellDto.ShellExecuteResult> executeCommand(
            @RequestBody ShellDto.ShellExecuteRequest request) {
        ShellDto.ShellExecuteResult result = shellService.executeCommand(
                request.getSessionId(), request.getExecDir(), request.getCommand());
        return ApiResponse.success(result, "命令执行成功");
    }

    @GetMapping("/{sessionId}/output")
    public ApiResponse<ShellDto.ShellReadResult> readOutput(@PathVariable String sessionId) {
        return ApiResponse.success(shellService.readOutput(sessionId), "获取Shell输出成功");
    }

    @PostMapping("/{sessionId}/kill")
    public ApiResponse<Void> killProcess(@PathVariable String sessionId) {
        shellService.terminateSession(sessionId);
        return ApiResponse.success("Process terminated");
    }
}

