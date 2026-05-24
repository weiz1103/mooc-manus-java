package com.imooc.manus.springai.config;

/**
 * Spring AI 多智能体流程配置参数。
 * 对应Python中的 AgentConfig 和相关配置项。
 *
 * <p>
 * 使用 Java Record 保证不可变性和强类型，避免 Map&lt;String, Object&gt;。
 * </p>
 *
 * @param modelName     LLM 模型名称（如 "gpt-4o"）
 * @param maxRetries    单次 LLM/工具调用最大重试次数
 * @param maxIterations 单个 Agent 调用的最大工具调用迭代次数
 * @param retryIntervalMs 重试间隔（毫秒）
 *
 * @author thezehui@gmail.com
 */
public record SpringAIFlowConfig(
        String modelName,
        int maxRetries,
        int maxIterations,
        long retryIntervalMs
) {
    /**
     * 默认配置：
     * - 模型名称：gpt-4o
     * - 最大重试次数：3
     * - 最大迭代次数：30
     * - 重试间隔：1000ms
     */
    public static SpringAIFlowConfig defaultConfig() {
        return new SpringAIFlowConfig("gpt-4o", 3, 30, 1000L);
    }

    /**
     * 创建自定义配置
     *
     * @param modelName     模型名称
     * @param maxRetries    最大重试次数
     * @param maxIterations 最大迭代次数
     * @return 新配置实例
     */
    public static SpringAIFlowConfig of(String modelName, int maxRetries, int maxIterations) {
        return new SpringAIFlowConfig(modelName, maxRetries, maxIterations, 1000L);
    }
}

