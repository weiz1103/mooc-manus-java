package io.github.weiz1103.agentflow.api.infrastructure.config;

import io.github.weiz1103.agentflow.api.domain.model.appconfig.A2AServerConfig;
import io.github.weiz1103.agentflow.api.domain.model.appconfig.MCPServerConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过 application.yml 。AgentFlow.* 绑定。
 *

 */
@Data
@Configuration
@ConfigurationProperties(prefix = "AgentFlow")
public class AppProperties {

    /** LLM 配置，统一。Java application.yml 读取 */
    private LlmProperties llm = new LlmProperties();

    /** Agent 运行参数 */
    private AgentProperties agent = new AgentProperties();

    /** MCP 服务配置 */
    private McpProperties mcp = new McpProperties();

    /** A2A 服务配置 */
    private A2aProperties a2a = new A2aProperties();

    /** 沙箱配置 */
    private SandboxProperties sandbox = new SandboxProperties();

    /** 腾讯云COS配置 */
    private CosProperties cos = new CosProperties();

    /** Redis配置额外属。*/
    private RedisProperties redis = new RedisProperties();

    @Data
    public static class LlmProperties {
        /** 模型基础URL地址 */
        private String baseUrl = "https://api.deepseek.com";
        /** 模型API秘钥 */
        private String apiKey = "";
        /** 模型名字 */
        private String modelName = "deepseek-chat";
        /** 温度 */
        private double temperature = 0.7;
        /** 最大输出token。*/
        private int maxTokens = 8192;
    }

    @Data
    public static class AgentProperties {
        /** Agent最大迭代次。*/
        private int maxIterations = 100;
        /** 最大重试次。*/
        private int maxRetries = 3;
        /** 最大搜索结果条。*/
        private int maxSearchResults = 10;
        /** 是否启用后台任务执行模式 */
        private boolean asyncExecutionEnabled = true;
        /** Worker 轮询任务分发流的阻塞时间 */
        private int workerPollIntervalMs = 1000;
        /** 任务分发 Stream Key */
        private String dispatchStreamKey = "AgentFlow:agent:dispatch";
        /** 任务分发 consumer group */
        private String dispatchConsumerGroup = "AgentFlow-agent-workers";
        /** worker 认领 stale pending 消息的最小空闲时。*/
        private int dispatchClaimIdleMs = 30000;
        /** 单次认领 stale pending 消息数量 */
        private int dispatchClaimBatchSize = 10;
        /** 重复工具调用达到该阈值后标记为疑似循。*/
        private int repeatedToolCallThreshold = 3;
        /** 单个 session 每分钟最多允许发起的消息次数。 表示不限。*/
        private int sessionMessageRatePerMinute = 20;
        /** 单个 session 24 小时内最多允许提交的任务数，0 表示不限。*/
        private int sessionTaskQuotaPerDay = 200;
        /** 单个 session 同时允许处于活动态的任务数，0 表示不限。*/
        private int sessionActiveTaskLimit = 1;
    }

    @Data
    public static class McpProperties {
        /** MCP 服务列表 */
        private Map<String, MCPServerConfig> servers = new LinkedHashMap<>();
    }

    @Data
    public static class A2aProperties {
        /** A2A 服务列表 */
        private List<A2AServerConfig> servers = new ArrayList<>();
    }

    @Data
    public static class SandboxProperties {
        private String address = "";
        private String image = "agentflow-sandbox:latest";
        /** 沙箱容器名前缀 */
        private String namePrefix = "agentflow-sandbox";
        /** 沙箱TTL（分钟） */
        private int ttlMinutes = 60;
        /** Docker网络 */
        private String network = "bridge";
        /** Chrome启动参数 */
        private String chromeArgs;
        /** HTTP代理 */
        private String httpsProxy;
        /** HTTP代理 */
        private String httpProxy;
        /** NO_PROXY */
        private String noProxy;
    }

    @Data
    public static class CosProperties {
        /** 腾讯云COS SecretId */
        private String secretId = "";
        /** 腾讯云COS SecretKey */
        private String secretKey = "";
        /** 腾讯云COS Region */
        private String region = "ap-guangzhou";
        /** 腾讯云COS Bucket */
        private String bucket = "";
    }

    @Data
    public static class RedisProperties {
        /** Redis Stream过期时间（秒。*/
        private int streamTtlSeconds = 86400;
    }
}



