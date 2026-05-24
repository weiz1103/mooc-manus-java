package com.imooc.manus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * MoocManus API 服务入口。
 * Java 重构版，对应原 Python FastAPI 应用。
 */
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
public class ManusApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManusApiApplication.class, args);
    }
}
