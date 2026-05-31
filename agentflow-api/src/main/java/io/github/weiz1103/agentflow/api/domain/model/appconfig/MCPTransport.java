package io.github.weiz1103.agentflow.api.domain.model.appconfig;

/**
 * MCP传输类型枚举。
 * @author zhuang03@qq.com
 * @date 2026-05-25 21:01:25
 */
public enum MCPTransport {
    /** 本地输入输出 */
    STDIO("stdio"),
    /** 流式事件 */
    SSE("sse"),
    /** 流式HTTP */
    STREAMABLE_HTTP("streamable_http");

    private final String value;

    MCPTransport(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}


