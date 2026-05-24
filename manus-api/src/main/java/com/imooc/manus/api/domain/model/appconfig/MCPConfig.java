package com.imooc.manus.api.domain.model.appconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用MCP配置。
 * 对应Python中的 MCPConfig Pydantic BaseModel。
 *
 * @author thezehui@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MCPConfig {

    @JsonProperty("mcpServers")
    private Map<String, MCPServerConfig> mcpServers = new HashMap<>();

    public MCPConfig() {}

    public Map<String, MCPServerConfig> getMcpServers() { return mcpServers; }
    public void setMcpServers(Map<String, MCPServerConfig> mcpServers) { this.mcpServers = mcpServers; }
}

