package io.github.weiz1103.agentflow.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 沙箱服务入口。
 */
@SpringBootApplication
public class SandboxServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandboxServerApplication.class, args);
    }
}

