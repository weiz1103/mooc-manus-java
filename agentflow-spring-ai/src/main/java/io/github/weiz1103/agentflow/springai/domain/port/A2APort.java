package io.github.weiz1103.agentflow.springai.domain.port;

import io.github.weiz1103.agentflow.common.model.ToolResult;

/**
 * A2A（Agent-to-Agent）工具端口。
 * 提供远程 Agent 卡片查询与调用能力。
 * @author zhuang03@qq.com
 * @date 2026-05-31 03:43:55
 */
public interface A2APort {

    /**
     * 获取所有可远程调用。Agent 卡片信息。
     *
     * @return 工具结果（包。Agent 列表。
     */
    ToolResult<Object> getRemoteAgentCards();

    /**
     * 根据 Agent id 。query 调用远程 Agent。
     *
     * @param id    远程 Agent id
     * @param query 分配给远。Agent 的任务描。
     * @return 工具结果
     */
    ToolResult<Object> callRemoteAgent(String id, String query);
}


