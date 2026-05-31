package io.github.weiz1103.agentflow.springai.domain.port;

import io.github.weiz1103.agentflow.common.model.ToolResult;

import java.util.List;
import java.util.Map;

/**
 * MCP（Model Context Protocol）工具端口。
 * agentflow-spring-ai 通过此接口调用外。MCP 工具。
 * 具体。MCP 客户端管理由 agentflow-api 层注入。
 * @author zhuang03@qq.com
 * @date 2026-05-30 04:57:09
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


