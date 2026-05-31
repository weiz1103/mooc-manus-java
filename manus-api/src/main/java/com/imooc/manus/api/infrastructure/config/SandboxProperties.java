package com.imooc.manus.api.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 沙箱连接配置，对应 HTTP 沙箱客户端参数。
 * 通过 application.yml 的 manus.sandbox-client.* 绑定，
 * 避免与 Docker 沙箱创建参数 {@code manus.sandbox.*} 冲突。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "manus.sandbox-client")
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-26 17:46:55
 */
public class SandboxProperties {

    /** 沙箱服务器基础 URL */
    private String baseUrl = "http://localhost:8081";

    /** 连接超时（秒） */
    private int connectTimeoutSeconds = 10;

    /** 读取超时（秒）*/
    private int readTimeoutSeconds = 300;
}
