package com.imooc.manus.sandbox.controller;

import com.imooc.manus.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 提供健康检查端点，供 manus-api 的 SandboxClient 探活。
 */
@RestController
@RequestMapping("/api/supervisor")
@Slf4j
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 10:37:37
 */
public class SupervisorController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "healthy",
                "timestamp", System.currentTimeMillis()
        ), "Sandbox is healthy");
    }
}
