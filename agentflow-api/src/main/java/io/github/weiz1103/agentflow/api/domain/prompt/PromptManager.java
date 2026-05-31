package io.github.weiz1103.agentflow.api.domain.prompt;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 提示词统一管理与渲染引。
 * 核心逻辑：利。Spring AI 。SystemPromptTemplate 动态注入变量，生成不可变的 Message 对象
 */
@Component
public class PromptManager {

    /**
     * 生成 Planner Agent 。System Message
     *
     * @param userInput 用户的原始请。
     * @return 渲染后的 Spring AI SystemMessage 对象
     */
    public Message buildPlannerSystemMessage(String userInput) {
        // 防御性编。
        if (userInput == null || userInput.isBlank()) {
            throw new IllegalArgumentException("User input cannot be empty when building Planner Prompt");
        }

        SystemPromptTemplate template = new SystemPromptTemplate(PromptConstants.PLANNER_SYSTEM_PROMPT);
        return template.createMessage(Map.of("userInput", userInput));
    }

    /**
     * 生成 ReAct Agent 。System Message
     *
     * @param currentStep      当前需要执行的任务步骤描述
     * @param toolDescriptions 当前注入工具包的详细描述
     * @return 渲染后的 Spring AI SystemMessage 对象
     */
    public Message buildReActSystemMessage(String currentStep, String toolDescriptions) {
        // 防御性编。
        if (currentStep == null || currentStep.isBlank()) {
            throw new IllegalArgumentException("Current step cannot be empty");
        }

        // 保证 toolDescriptions 即使为空也不会导致渲染崩。
        String safeToolDescriptions = (toolDescriptions == null) ? "暂无可用工具" : toolDescriptions;

        SystemPromptTemplate template = new SystemPromptTemplate(PromptConstants.REACT_SYSTEM_PROMPT);
        return template.createMessage(Map.of(
                "currentStep", currentStep,
                "toolDescriptions", safeToolDescriptions
        ));
    }
}
