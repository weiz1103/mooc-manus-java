package com.imooc.manus.sandbox.controller;

import com.imooc.manus.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 沙箱监控控制器，对应 Python supervisor 功能。
 * 提供健康检查端点，供 manus-api 的 SandboxClient 探活。
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
