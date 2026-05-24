package com.imooc.manus.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 沙箱服务入口。
 * 提供隔离的浏览器、Shell、文件操作环境，对应 Python sandbox 服务。
 */
@SpringBootApplication
public class SandboxServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandboxServerApplication.class, args);
    }
}
