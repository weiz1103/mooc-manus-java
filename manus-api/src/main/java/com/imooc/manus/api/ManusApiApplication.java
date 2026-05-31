package com.imooc.manus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * MoocManus API 服务入口。
 */
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-25 23:03:54
 */
public class ManusApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManusApiApplication.class, args);
    }
}
