package com.imooc.manus.api.domain.model.appconfig;

/**
 * MCP传输类型枚举。
 * 对应Python中的 MCPTransport 枚举。
 *
 * @author thezehui@gmail.com
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

