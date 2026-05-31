package com.imooc.manus.springai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.imooc.manus.common.event.*;
import com.imooc.manus.springai.config.SpringAIFlowConfig;
import com.imooc.manus.springai.memory.AgentMemoryStore;
import com.imooc.manus.springai.model.AgentMessage;
import com.imooc.manus.springai.model.ExecutionStatus;
import com.imooc.manus.springai.model.Plan;
import com.imooc.manus.springai.model.Step;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 基于 ReAct 架构的执行 Agent（Spring AI 实现）。
 *
 * <p>
 * 功能：
 * - execute_step：根据传递的消息+规划+子步骤，执行相应的子步骤
 * - summarize：汇总历史的消息并生成最终回答/附件
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-24 22:30:38
 */
public class SpringAIReActAgent extends BaseSpringAIAgent {

    private static final String NAME = "react";

    // ======================== Prompt 常量 ========================

    /** ReActAgent 系统预设 prompt */
    private static final String REACT_SYSTEM_PROMPT = """

你是一个任务执行智能体（Agent）, 你需要按照以下步骤完成任务:

1. **分析事件**：理解用户需求和当前状态，重点关注最新的用户消息以及上一步的执行结果。
2. **选择工具**：根据当前状态和任务规划，选择下一个需要调用的工具。
3. **等待执行**：选定的工具操作将由沙箱环境实际执行（你只需生成调用指令）。
4. **循环迭代**：每次迭代原则上只选择一个工具调用，耐心重复上述步骤，直到任务完成。
5. **提交结果**：将最终结果发送给用户，结果必须要少且具体。
""";

    /** 执行子步骤提示词模板 */
    private static final String EXECUTION_PROMPT = """
你正在执行任务：
{step}

注意事项：
- **是你来执行这个任务，而不是用户。**不要告诉用户"如何做"，而是直接通过工具"去做"。
- **必须使用用户消息中使用的语言（Working Language）来执行任务和回复。**
- 必须使用 `message_notify_user` 工具向用户通报进度，内容限制在一句话以内：
    - 你打算使用什么工具，以及用它做什么；
    - 或者你通过工具完成了什么；
    - 简明扼要地告知当前动作。
- 如果你需要用户提供输入或需要获取浏览器的控制权，必须使用 `message_ask_user` 工具向用户提问。
- 再次强调：直接交付最终结果，而不是提供待办事项列表、建议或计划。

返回格式要求：
- 必须返回符合以下 TypeScript 接口定义的 JSON 格式（当执行完成时）
- 每次工具调用只能调用一个工具

TypeScript 接口定义（最终步骤结果）：
```typescript
interface StepResult {{
  /** 是否成功完成步骤 **/
  success: boolean;
  /** 步骤执行结果描述，尽可能详细 **/
  result: string;
  /** 步骤执行结果中的附件路径列表（如果有） **/
  attachments: string[];
}}
```

用户消息：
{message}

附件：
{attachments}

工作语言：
{language}
""";

    /** 汇总提示词 */
    private static final String SUMMARIZE_PROMPT = """
任务已完成，请根据所有已执行步骤的结果，生成最终完整的答复/交付物。

要求：
- 汇总所有执行步骤的成果，生成最终结果给用户
- 如果涉及附件（如生成的文件、报告等），请在 attachments 中包含完整的文件绝对路径
- 使用与用户一致的语言进行回复

返回格式要求：
- 必须返回符合以下 TypeScript 接口定义的 JSON 格式

TypeScript 接口定义：
```typescript
interface SummarizeResult {{
  /** 最终结果汇总消息（给用户看的） **/
  message: string;
  /** 最终结果中的附件路径列表（如果有） **/
  attachments: string[];
}}
```
""";

    /**
     * 构造执行 Agent。
     *
     * @param sessionId     会话id
     * @param memoryStore   记忆持久化存储
     * @param chatClient    Spring AI ChatClient
     * @param toolCallbacks 工具回调列表（FileTool、ShellTool、BrowserTool 等）
     * @param config        流程配置
     */
    public SpringAIReActAgent(
            String sessionId,
            AgentMemoryStore memoryStore,
            ChatClient chatClient,
            List<ToolCallback> toolCallbacks,
            SpringAIFlowConfig config
    ) {
        super(sessionId, memoryStore, chatClient, toolCallbacks, config);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected String getSystemPrompt() {
        return SystemPrompts.SYSTEM_PROMPT + REACT_SYSTEM_PROMPT;
    }

    /**
     * 根据传递的消息+规划+子步骤，执行相应的子步骤。
     *
     * <p>
     * 流程：
     * 1. 根据传递的内容生成执行消息
     * 2. 更新步骤执行状态为 RUNNING 并发送 StepEvent(STARTED)
     * 3. 调用 invokeLlm 获取执行结果（json_object 格式，允许工具调用）
     * 4. 如果 invokeLlm 返回 null，说明遇到了 WaitEvent（已由 invokeLlm 发送），直接返回 true
     * 5. 解析执行结果 JSON 为 Step 对象
     * 6. 更新步骤状态，发送 StepEvent(COMPLETED)
     * 7. 如果步骤有结果，发送 MessageEvent
     * </p>
     *
     * @param plan    当前规划
     * @param step    当前要执行的子步骤
     * @param message 用户消息
     * @param emitter 事件发射器
     * @return true 表示遇到了 WaitEvent（需要等待用户输入），false 表示步骤正常完成
     */
    public boolean executeStep(Plan plan, Step step, AgentMessage message, Consumer<BaseEvent> emitter) {
        // 1. 根据传递的内容生成执行消息
        String query = EXECUTION_PROMPT
                .replace("{message}", message.message())
                .replace("{attachments}", String.join("\n", message.attachments()))
                .replace("{language}", plan.getLanguage() != null ? plan.getLanguage() : "中文")
                .replace("{step}", step.getDescription() != null ? step.getDescription() : "");

        step.setStatus(ExecutionStatus.RUNNING);
        emitter.accept(StepEvent.builder()
                .step(toStepData(step))
                .status(StepEvent.StepEventStatus.STARTED)
                .build());

        // 3. 调用 invokeLlm（json_object 格式，允许工具调用）
        AssistantMessage assistantMsg = invokeLlm(query, "json_object", null, emitter);

        // 4. 如果 invokeLlm 返回 null，说明是 WaitEvent 或错误（WaitEvent 已在 invokeLlm 中发送）
        if (assistantMsg == null) {
            // 步骤未完成，等待用户输入
            return true; // 信号：需要等待用户输入
        }

        // 5. 解析执行结果 JSON 为 Step 对象
        String content = assistantMsg.getText();
        logger.info("执行Agent生成消息: {}...",
                content != null && content.length() > 100 ? content.substring(0, 100) : content);

        try {
            Step resultStep = OBJECT_MAPPER.readValue(content, Step.class);

            // 6. 更新子步骤的数据（success、result、attachments）
            step.setStatus(ExecutionStatus.COMPLETED);
            step.setSuccess(resultStep.getSuccess());
            step.setResult(resultStep.getResult());
            step.setAttachments(resultStep.getAttachments());

            emitter.accept(StepEvent.builder()
                    .step(toStepData(step))
                    .status(StepEvent.StepEventStatus.COMPLETED)
                    .build());

            // 8. 已移除 step.getResult() 的广播。
            // 因为在提示词中，LLM 已经被要求通过 message_notify_user 与用户通讯。
            // 如果这里再把内部使用的 step.result 发送给用户，会导致界面出现重复消息。
        } catch (Exception e) {
            logger.error("解析步骤结果失败: {}", e.getMessage());
            // 解析失败时标记步骤为失败
            step.setStatus(ExecutionStatus.FAILED);
            step.setError("步骤结果解析失败: " + e.getMessage());
            emitter.accept(StepEvent.builder()
                    .step(toStepData(step))
                    .status(StepEvent.StepEventStatus.FAILED)
                    .build());
        }

        // 9. 循环迭代完成后代表子步骤已实现，更新状态
        step.setStatus(ExecutionStatus.COMPLETED);
        return false;
    }

    /**
     * 调用 Agent 汇总历史的消息并生成最终回答/附件。
     *
     * <p>
     * 流程：
     * 1. 构建汇总请求 query
     * 2. 调用 invoke 方法获取 Agent 生成的事件
     * 3. 判断事件类型是否是消息事件，如果是则表示 Agent 结构化生成汇总内容
     * 4. 解析输出内容
     * 5. 返回消息事件并将消息+附件进行相应
     * </p>
     *
     * @param emitter 事件发射器
     */
    public void summarize(Consumer<BaseEvent> emitter) {
        // 1. 调用 invokeLlm 获取汇总内容
        AssistantMessage assistantMsg = invokeLlm(SUMMARIZE_PROMPT, "json_object", null, emitter);
        if (assistantMsg == null) return;

        // 2. 记录日志并解析输出内容
        String content = assistantMsg.getText();
        logger.info("执行Agent生成汇总内容: {}...",
                content != null && content.length() > 100 ? content.substring(0, 100) : content);

        try {
            // 3. 将解析数据转换为 Message 对象
            java.util.Map<String, Object> resultMap = OBJECT_MAPPER.readValue(
                    content, new TypeReference<java.util.Map<String, Object>>() {});

            String messageText = (String) resultMap.getOrDefault("message", "");
            Object attachmentsObj = resultMap.get("attachments");
            List<String> attachments = new ArrayList<>();
            if (attachmentsObj instanceof List) {
                for (Object a : (List<?>) attachmentsObj) {
                    if (a != null) attachments.add(a.toString());
                }
            }

            emitter.accept(MessageEvent.builder()
                    .role("assistant")
                    .message(messageText)
                    .attachments(attachments)
                    .build());
        } catch (Exception e) {
            logger.error("解析汇总结果失败: {}", e.getMessage());
            // 解析失败时直接返回原始内容
            emitter.accept(MessageEvent.builder()
                    .role("assistant")
                    .message(content != null ? content : "")
                    .build());
        }
    }

    // ===================== 工具方法 =====================

    /**
     * 将 Step 领域模型转换为 manus-common StepEvent.StepData（用于 SSE 传输）
     */
    private StepEvent.StepData toStepData(Step step) {
        return StepEvent.StepData.builder()
                .id(step.getId())
                .description(step.getDescription())
                .status(step.getStatus() != null ? step.getStatus().name().toLowerCase() : "pending")
                .build();
    }
}

