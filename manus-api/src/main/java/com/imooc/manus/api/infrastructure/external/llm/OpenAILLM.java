package com.imooc.manus.api.infrastructure.external.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.domain.external.LLM;
import com.imooc.manus.api.domain.model.appconfig.LLMConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * 基于OpenAI SDK/兼容OpenAI格式的LLM调用类。
 * 对应Python中的 OpenAILLM 类。
 * <p>
 * 使用Spring RestClient（同步HTTP）调用OpenAI Chat Completions API，
 * 运行在Java虚拟线程中，因此阻塞调用不影响系统响应性。
 * </p>
 *
 * @author thezehui@gmail.com
 */
public class OpenAILLM implements LLM {

    private static final Logger logger = LoggerFactory.getLogger(OpenAILLM.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int TIMEOUT_SECONDS = 3600;

    private final RestClient restClient;
    private final String modelName;
    private final double temperature;
    private final int maxTokens;

    /**
     * 构造函数，完成异步OpenAI客户端的创建和参数初始化
     *
     * @param llmConfig LLM配置
     */
    public OpenAILLM(LLMConfig llmConfig) {
        // 1.初始化RestClient（对应Python的AsyncOpenAI客户端创建）
        this.restClient = RestClient.builder()
                .baseUrl(llmConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + llmConfig.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // 2.完成其他参数的存储
        this.modelName = llmConfig.getModelName();
        this.temperature = llmConfig.getTemperature();
        this.maxTokens = llmConfig.getMaxTokens();
    }

    @Override
    public String getModelName() { return modelName; }

    @Override
    public double getTemperature() { return temperature; }

    @Override
    public int getMaxTokens() { return maxTokens; }

    /**
     * 使用RestClient向LLM发起请求（该步骤在虚拟线程中阻塞执行）。
     * 对应Python的 OpenAILLM.invoke()
     *
     * @param messages       消息列表（OpenAI格式）
     * @param tools          工具列表（OpenAI function schema格式），可为null
     * @param responseFormat 响应格式（如{"type": "json_object"}），可为null
     * @param toolChoice     工具选择策略，可为null
     * @return LLM返回的消息Map
     * @throws RuntimeException 调用失败时抛出
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> invoke(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            Map<String, Object> responseFormat,
            String toolChoice
    ) {
        try {
            // 1.构建请求体
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("messages", messages);

            if (responseFormat != null) {
                requestBody.put("response_format", responseFormat);
            }

            // 2.检测是否传递了工具列表
            if (tools != null && !tools.isEmpty()) {
                logger.info("调用OpenAI客户端向LLM发起请求并携带工具信息: {}", modelName);
                requestBody.put("tools", tools);
                // 关闭并行工具调用(deepseek没有这个参数，其他模型可能需要)
                requestBody.put("parallel_tool_calls", false);
                if (toolChoice != null) {
                    requestBody.put("tool_choice", toolChoice);
                }
            } else {
                // 3.未传递工具则删除tools/tool_choice等参数
                logger.info("调用OpenAI客户端向LLM发起请求未携带工具: {}", modelName);
            }

            // 4.使用RestClient发起请求（运行在虚拟线程中，阻塞不影响响应性）
            Map<String, Object> response = restClient.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new RuntimeException("LLM返回了空响应");
            }

            // 5.处理响应数据并返回
            logger.info("OpenAI客户端返回内容已接收, model: {}", modelName);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("LLM返回了空的choices列表");
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            if (message == null) {
                throw new RuntimeException("LLM返回消息为null");
            }

            return message;
        } catch (Exception e) {
            logger.error("调用OpenAI客户端发生错误: {}", e.getMessage());
            throw new RuntimeException("调用OpenAI客户端向LLM发起请求出错: " + e.getMessage(), e);
        }
    }
}

