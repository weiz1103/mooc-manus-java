package com.imooc.manus.api.domain.model.appconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LLM提供商配置。
 *

 */
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 18:10:10
 */
public class LLMConfig {

    /** 模型基础URL地址 */
    @JsonProperty("base_url")
    private String baseUrl = "https://api.deepseek.com";

    /** 模型API秘钥 */
    @JsonProperty("api_key")
    private String apiKey = "";

    /** 模型名字，默认使用deepseek-reasoner带推理的模型，传入tools会自动切换到deepseek-chat */
    @JsonProperty("model_name")
    private String modelName = "deepseek-reasoner";

    /** 温度，默认设置为0.7 */
    @JsonProperty("temperature")
    private double temperature = 0.7;

    /** 最大输出token数，默认设置为deepseek-chat模型的最大输出限制 */
    @JsonProperty("max_tokens")
    private int maxTokens = 8192;

    public LLMConfig() {}

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
}

