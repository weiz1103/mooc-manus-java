package com.imooc.manus.springai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.springai.domain.port.SearchEnginePort;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 搜索工具服务，提供全网搜索能力。
 * <p>
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-30 16:56:49
 */
public class SearchToolService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SearchEnginePort searchEngine;

    public SearchToolService(SearchEnginePort searchEngine) {
        this.searchEngine = searchEngine;
    }

    @Tool(name = "search_web", description = "全网搜索引擎工具。当需要获取实时信息（如突发新闻、天气）、" +
            "补充内部知识库未覆盖的内容或进行事实核查时使用。该工具会返回相关的网页摘要和链接。")
    public String searchWeb(
            @ToolParam(description = "搜索查询内容") String query,
            @ToolParam(description = "日期范围限制（可选，如 past_week）", required = false) String dateRange
    ) {
        return serialize(searchEngine.search(query, dateRange));
    }

    private String serialize(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"序列化失败\"}";
        }
    }
}

