package com.imooc.manus.springai.domain.port;

import com.imooc.manus.common.model.ToolResult;

import java.util.List;
import java.util.Map;

/**
 * MCP（Model Context Protocol）工具端口。
 * manus-spring-ai 通过此接口调用外部 MCP 工具，
 * 具体的 MCP 客户端管理由 manus-api 层注入。
 */
public interface McpPort {

    /**
     * 获取所有可用的 MCP 工具 Schema（OpenAI function-calling 格式）。
     *
     * @return 工具定义列表
     */
    List<Map<String, Object>> getTools();

    /**
     * 调用指定 MCP 工具。
     *
     * @param toolName 工具名称
     * @param kwargs   工具参数
     * @return 工具执行结果
     */
    ToolResult<Object> invoke(String toolName, Map<String, Object> kwargs);
}

