package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.imooc.manus.api.domain.model.search.SearchResultItem;

import java.util.List;

/**
 * 工具扩展内容模型（浏览器、搜索、Shell、文件、MCP、A2A）
 * 对应Python中的 BrowserToolContent, SearchToolContent, 等
 *
 * @author thezehui@gmail.com
 */
public final class ToolContent {

    private ToolContent() {}

    /** 浏览器工具扩展内容 */
    public record BrowserToolContent(
            @JsonProperty("screenshot") String screenshot
    ) {}

    /** 搜索工具内容 */
    public record SearchToolContent(
            @JsonProperty("results") List<SearchResultItem> results
    ) {}

    /** Shell工具内容 */
    public record ShellToolContent(
            @JsonProperty("console") Object console
    ) {}

    /** 文件工具内容 */
    public record FileToolContent(
            @JsonProperty("content") String content
    ) {}

    /** MCP工具内容 */
    public record MCPToolContent(
            @JsonProperty("result") Object result
    ) {}

    /** A2A智能体工具内容 */
    public record A2AToolContent(
            @JsonProperty("a2a_result") Object a2aResult
    ) {}
}

