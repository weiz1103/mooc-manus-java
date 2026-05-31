package com.imooc.manus.api.domain.model.appconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * MCP服务配置。
 *

 */
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-29 03:10:36
 */
public class MCPServerConfig {

    /** 传输协议，默认为streamable_http */
    @JsonProperty("transport")
    private MCPTransport transport = MCPTransport.STREAMABLE_HTTP;

    /** 是否开启，默认为True */
    @JsonProperty("enabled")
    private boolean enabled = true;

    /** 服务器描述 */
    @JsonProperty("description")
    private String description;

    /** 环境变量配置 */
    @JsonProperty("env")
    private Map<String, Object> env;

    // stdio配置
    /** 启用命令 */
    @JsonProperty("command")
    private String command;

    /** 命令参数 */
    @JsonProperty("args")
    private List<String> args;

    // streamable_http&sse配置
    /** MCP服务URL地址 */
    @JsonProperty("url")
    private String url;

    /** MCP服务请求头 */
    @JsonProperty("headers")
    private Map<String, Object> headers;

    public MCPServerConfig() {}

    // Getters & Setters
    public MCPTransport getTransport() { return transport; }
    public void setTransport(MCPTransport transport) { this.transport = transport; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getEnv() { return env; }
    public void setEnv(Map<String, Object> env) { this.env = env; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public List<String> getArgs() { return args; }
    public void setArgs(List<String> args) { this.args = args; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, Object> getHeaders() { return headers; }
    public void setHeaders(Map<String, Object> headers) { this.headers = headers; }
}

