package com.imooc.manus.springai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring AI Agent 配置属性。
 * <p>
 * 配置前缀：{@code manus.agent}
 * </p>
 *
 * <p>示例 application.yml：</p>
 * <pre>{@code
 * manus:
 *   agent:
 *     max-iterations: 100
 *     max-retries: 3
 *     retry-interval-ms: 1000
 * }</pre>
 *
 * <p><b>扩展点：</b> 新增配置项后在此类中添加对应字段并更新 application.yml 默认值。</p>
 */
@ConfigurationProperties(prefix = "manus.agent")
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-29 16:04:36
 */
public class ManusAgentProperties {

    /** Agent 最大迭代次数（每轮对话中工具调用 + 思考的最大轮次） */
    private int maxIterations = 100;

    /** 调用 LLM / 工具失败时的最大重试次数 */
    private int maxRetries = 3;

    /** 重试间隔（毫秒） */
    private long retryIntervalMs = 1000L;

    /** 最大搜索结果条数 */
    private int maxSearchResults = 10;

    // ---- getters / setters ----

    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public long getRetryIntervalMs() { return retryIntervalMs; }
    public void setRetryIntervalMs(long retryIntervalMs) { this.retryIntervalMs = retryIntervalMs; }

    public int getMaxSearchResults() { return maxSearchResults; }
    public void setMaxSearchResults(int maxSearchResults) { this.maxSearchResults = maxSearchResults; }
}

