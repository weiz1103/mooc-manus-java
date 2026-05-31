package io.github.weiz1103.agentflow.api.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 沙箱连接配置，对。HTTP 沙箱客户端参数。
 * 通过 application.yml 。AgentFlow.sandbox-client.* 绑定。
 * 避免。Docker 沙箱创建参数 {@code AgentFlow.sandbox.*} 冲突。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "AgentFlow.sandbox-client")
public class SandboxProperties {

    /** 沙箱服务器基础 URL */
    private String baseUrl = "http://localhost:8081";

    /** 连接超时（秒。*/
    private int connectTimeoutSeconds = 10;

    /** 读取超时（秒。/
    private int readTimeoutSeconds = 300;
}


