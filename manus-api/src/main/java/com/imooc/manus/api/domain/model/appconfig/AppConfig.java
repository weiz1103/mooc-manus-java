package com.imooc.manus.api.domain.model.appconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 应用配置信息，包含AgentConfig、LLM提供商配置、MCP配置、A2A配置。
 * 对应Python中的 AppConfig Pydantic BaseModel。
 *
 * @author thezehui@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    /** 语言模型配置 */
    @JsonProperty("llm_config")
    private LLMConfig llmConfig;

    /** Agent通用配置 */
    @JsonProperty("agent_config")
    private AgentConfig agentConfig;

    /** MCP服务配置 */
    @JsonProperty("mcp_config")
    private MCPConfig mcpConfig;

    /** A2A服务配置 */
    @JsonProperty("a2a_config")
    private A2AConfig a2aConfig;

    public AppConfig() {
        this.llmConfig = new LLMConfig();
        this.agentConfig = new AgentConfig();
        this.mcpConfig = new MCPConfig();
        this.a2aConfig = new A2AConfig();
    }

    public LLMConfig getLlmConfig() { return llmConfig; }
    public void setLlmConfig(LLMConfig llmConfig) { this.llmConfig = llmConfig; }

    public AgentConfig getAgentConfig() { return agentConfig; }
    public void setAgentConfig(AgentConfig agentConfig) { this.agentConfig = agentConfig; }

    public MCPConfig getMcpConfig() { return mcpConfig; }
    public void setMcpConfig(MCPConfig mcpConfig) { this.mcpConfig = mcpConfig; }

    public A2AConfig getA2aConfig() { return a2aConfig; }
    public void setA2aConfig(A2AConfig a2aConfig) { this.a2aConfig = a2aConfig; }
}

