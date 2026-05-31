package io.github.weiz1103.agentflow.sandbox.controller;

import io.github.weiz1103.agentflow.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 提供健康检查端点，。agentflow-api 。SandboxClient 探活。
 */
@RestController
@RequestMapping("/api/supervisor")
@Slf4j
public class SupervisorController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "healthy",
                "timestamp", System.currentTimeMillis()
        ), "Sandbox is healthy");
    }
}

