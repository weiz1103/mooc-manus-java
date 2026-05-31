package com.imooc.manus.api.domain.service.agent;

import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.springai.flow.SpringAIPlannerReActFlow;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 基于 Spring AI Planner+ReAct 架构的 Agent 执行策略。
 *
 * <p>适配器模式：将 {@link SpringAIPlannerReActFlow} 包装为统一的 {@link AgentStrategy}，
 * 屏蔽 Spring AI 框架细节，使上层 {@link AgentRunner} 与具体 AI 实现解耦。</p>
 */
@Component
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-30 12:29:14
 */
public class ReActAgentStrategy implements AgentStrategy {

    private final SpringAIPlannerReActFlow plannerFlow;

    public ReActAgentStrategy(SpringAIPlannerReActFlow plannerFlow) {
        this.plannerFlow = plannerFlow;
    }

    @Override
    public Flux<BaseEvent> execute(String sessionId, String message, List<String> attachments) {
        return plannerFlow.invoke(sessionId, message, attachments);
    }
}
