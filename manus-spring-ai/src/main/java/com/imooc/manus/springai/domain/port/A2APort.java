package com.imooc.manus.springai.domain.port;

import com.imooc.manus.common.model.ToolResult;

/**
 * A2A（Agent-to-Agent）工具端口。
 * 提供远程 Agent 卡片查询与调用能力。
 * @author zhuang03@qq.com
 * @date 2026-05-31 03:43:55
 */
public interface A2APort {

    /**
     * 获取所有可远程调用的 Agent 卡片信息。
     *
     * @return 工具结果（包含 Agent 列表）
     */
    ToolResult<Object> getRemoteAgentCards();

    /**
     * 根据 Agent id 和 query 调用远程 Agent。
     *
     * @param id    远程 Agent id
     * @param query 分配给远程 Agent 的任务描述
     * @return 工具结果
     */
    ToolResult<Object> callRemoteAgent(String id, String query);
}

