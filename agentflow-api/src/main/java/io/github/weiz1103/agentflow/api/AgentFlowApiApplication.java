package io.github.weiz1103.agentflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AgentFlow API 服务入口。
 */
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
public class AgentFlowApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentFlowApiApplication.class, args);
    }
}


