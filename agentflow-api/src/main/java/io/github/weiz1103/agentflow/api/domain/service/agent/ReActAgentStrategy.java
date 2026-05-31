package io.github.weiz1103.agentflow.api.domain.service.agent;

import io.github.weiz1103.agentflow.common.event.BaseEvent;
import io.github.weiz1103.agentflow.springai.flow.SpringAIPlannerReActFlow;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 基于 Spring AI Planner+ReAct 架构。Agent 执行策略。
 *
 * <p>适配器模式：。{@link SpringAIPlannerReActFlow} 包装为统一。{@link AgentStrategy}。
 * 屏蔽 Spring AI 框架细节，使上层 {@link AgentRunner} 与具。AI 实现解耦。</p>
 */
@Component
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

