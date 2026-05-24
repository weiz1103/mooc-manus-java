package com.imooc.manus.api.domain.model.appconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Agent通用配置。
 * 对应Python中的 AgentConfig Pydantic BaseModel。
 *
 * @author thezehui@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentConfig {

    /** Agent最大迭代次数 */
    @JsonProperty("max_iterations")
    private int maxIterations = 100;

    /** 最大重试次数 */
    @JsonProperty("max_retries")
    private int maxRetries = 3;

    /** 最大搜索结果条数 */
    @JsonProperty("max_search_results")
    private int maxSearchResults = 10;

    public AgentConfig() {}

    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public int getMaxSearchResults() { return maxSearchResults; }
    public void setMaxSearchResults(int maxSearchResults) { this.maxSearchResults = maxSearchResults; }
}

