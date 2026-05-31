package com.imooc.manus.api.domain.service.agent;

import com.imooc.manus.common.event.BaseEvent;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Agent 执行策略接口。
 *
 * <p>解耦应用层与具体 AI 框架（Spring AI / LangChain4j 等）。
 * 当前实现为基于 Spring AI Planner+ReAct 的 {@code ReActAgentStrategy}。</p>
 *
 * <p>如需切换 AI 框架或执行模式（如直接回答、固定工具流），
 * 只需新增此接口的实现并通过配置切换，不改动任何上层业务代码。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-27 09:28:41
 */
public interface AgentStrategy {

    /**
     * 执行一轮 Agent 推理，以响应式流的形式返回过程事件。
     *
     * @param sessionId   会话 ID
     * @param message     用户输入
     * @param attachments 附件文件路径列表
     * @return 事件流（plan / step / tool / message / wait / done / error）
     */
    Flux<BaseEvent> execute(String sessionId, String message, List<String> attachments);
}
