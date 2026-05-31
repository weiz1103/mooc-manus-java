package com.imooc.manus.springai.agent;

import com.imooc.manus.common.event.*;
import com.imooc.manus.springai.config.SpringAIFlowConfig;
import com.imooc.manus.springai.memory.AgentMemoryStore;
import com.imooc.manus.springai.model.Plan;
import com.imooc.manus.springai.model.Step;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 规划 Agent（Spring AI 实现）。
 *
 * <p>
 * 功能：将用户的任务/需求拆解成多个子步骤，根据已完成的子任务更新规划。
 * 特点：
 * - format = "json_object"：输出 JSON 格式规划
 * - tool_choice = "none"：不调用工具，只输出 JSON 内容
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-31 08:17:54
 */
public class SpringAIPlannerAgent extends BaseSpringAIAgent {

    private static final String NAME = "planner";

    // ======================== Prompt 常量（从 manus-api 迁移）========================

    /** PlannerAgent 系统预设 prompt（任务规划者角色定义） */
    private static final String PLANNER_SYSTEM_PROMPT = """

你是一个任务规划智能体 (Task Planner Agent), 你需要为任务创建或更新规划:
1. 分析用户的消息并理解用户的需求
2. 确定完成任务需要使用哪些工具
3. 根据用户的消息确定工作语言;
4. 生成规划的目标和步骤;
""";

    /** 创建 Plan 规划提示词模板，内部有 {message}+{attachments} 占位符 */
    private static final String CREATE_PLAN_PROMPT = """
你现在正在根据用户的消息创建一个规划:

注意：
- **你必须使用用户消息中使用的语言来执行任务**
- 你的规划必须简洁明了，不要添加任何不必要的细节
- 你的步骤必须是原子性且独立的，以便下一个执行者可以使用工具逐一执行它们
- 你需要判断任务是否可以拆分为多个步骤，如果可以，返回多个步骤；否则，返回单个步骤

返回格式要求：
- 必须返回符合以下 TypeScript 接口定义的 JSON 格式
- 必须包含指定的所有必填字段
- 如果判定任务不可行, 则"steps"返回空数组，"goal"返回空字符串

TypeScript 接口定义：
```typescript
interface CreatePlanResponse {{
  /** 对用户消息的回复以及对任务的思考，尽可能详细，使用用户的语言 **/
  message: string;
  /** 根据用户消息确定的工作语言 **/
  language: string;
  /** 步骤数组，每个步骤包含id和描述 **/
  steps: Array<{{
    /** 步骤标识符 **/
    id: string;
    /** 步骤描述 **/
    description: string;
  }}>;
  /** 根据上下文生成的规划目标 **/
  goal: string;
  /** 根据上下文生成的规划标题 **/
  title: string;
}}
```

JSON 输出示例:
{{
  "message": "用户回复消息",
  "goal": "目标描述",
  "title": "任务标题",
  "language": "zh",
  "steps": [
    {{
      "id": "1",
      "description": "步骤1描述"
    }}
  ]
}}

用户消息:
{message}

附件:
{attachments}
""";

    /** 更新 Plan 规划提示词模板，内部有 {plan} 和 {step} 占位符 */
    private static final String UPDATE_PLAN_PROMPT = """
你正在更新规划，你需要根据步骤的执行结果来更新规划：
{step}

注意：
- 你可以删除、添加或者修改规划步骤，但不要改变规划目标(goal)
- 如果变动不大，不要修改描述
- 仅重新规划后续**未完成**的步骤，不要更改已完成的步骤
- 输出的步骤 ID 应以第一个未完成步骤的 ID 开始，重新规划其后的步骤
- 如果步骤已完成或者不再必要，请将其删除
- 仔细阅读步骤结果以确定是否成功，如果不成功，请更改后续步骤
- 根据步骤结果，你需要相应地更新规划步骤

返回格式要求：
- 必须返回符合以下 TypeScript 接口定义的 JSON 格式
- 必须包含指定的所有必填字段

TypeScript接口定义：
```typescript
interface UpdatePlanResponse {{
  /** 更新后的未完成步骤数组 **/
  steps: Array<{{
    /** 步骤标识符 **/
    id: string;
    /** 步骤描述 **/
    description: string;
  }}>;
}}
```

JSON输出示例：
{{
  "steps": [
    {{
      "id": "1",
      "description": "步骤1描述"
    }}
  ]
}}

步骤 (step):
{step}

规划 (plan):
{plan}
""";

    /**
     * 构造规划 Agent。
     *
     * @param sessionId     会话id
     * @param memoryStore   记忆持久化存储
     * @param chatClient    Spring AI ChatClient
     * @param toolCallbacks 工具回调列表（PlannerAgent 不使用工具，但保持接口一致）
     * @param config        流程配置
     */
    public SpringAIPlannerAgent(
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
        return SystemPrompts.SYSTEM_PROMPT + PLANNER_SYSTEM_PROMPT;
    }

    /**
     * 根据用户传递的消息创建规划/计划，并通过 emitter 发送对应事件。
     *
     * <p>
     * 流程：
     * 1. 根据用户消息生成创建 plan 的提示词
     * 2. 调用 invoke（json_object 格式，禁用工具调用）获取 JSON 响应
     * 3. 解析 JSON 为 Plan 对象
     * 4. 发送 PlanEvent(CREATED) 事件
     * </p>
     *
     * @param message 用户消息（含文本和附件路径）
     * @param emitter 事件发射器
     * @return 创建的 Plan 对象，失败时返回 null
     */
    public Plan createPlan(com.imooc.manus.springai.model.AgentMessage message, Consumer<BaseEvent> emitter) {
        // 1. 根据用户传递的消息生成创建 plan 的提示词
        String query = CREATE_PLAN_PROMPT
                .replace("{message}", message.message())
                .replace("{attachments}", String.join("\n", message.attachments()));

        // 2. 调用 invokeLlm（json_object 格式，tool_choice=none，禁止调用工具）
        AssistantMessage assistantMsg = invokeLlm(query, "json_object", "none", emitter);
        if (assistantMsg == null) return null;

        // 3. 记录日志并解析 JSON
        String content = assistantMsg.getText();
        logger.info("PlannerAgent 生成消息: {}...",
                content != null && content.length() > 100 ? content.substring(0, 100) : content);

        Plan plan = parseJsonToObject(content, Plan.class);
        if (plan == null) {
            emitter.accept(ErrorEvent.builder().error("PlannerAgent 规划解析失败").build());
            return null;
        }

        PlanEvent planEvent = PlanEvent.builder()
                .plan(toPlanData(plan))
                .status(PlanEvent.PlanEventStatus.CREATED)
                .build();
        emitter.accept(planEvent);
        return plan;
    }

    /**
     * 根据传递的原有规划+子步骤更新规划事件。
     *
     * <p>
     * 流程：
     * 1. 使用 plan + step 创建更新 Plan 提示词
     * 2. 调用 invoke 获取 JSON 响应
     * 3. 解析 JSON，更新 plan.steps（保留已完成步骤，替换后续步骤）
     * 4. 发送 PlanEvent(UPDATED) 事件
     * </p>
     *
     * @param plan    当前规划（含已完成步骤）
     * @param step    刚执行完的步骤（含执行结果）
     * @param emitter 事件发射器
     */
    public void updatePlan(Plan plan, Step step, Consumer<BaseEvent> emitter) {
        try {
            // 1. 使用 plan + step 创建更新 Plan 提示词
            String query = UPDATE_PLAN_PROMPT
                    .replace("{step}", OBJECT_MAPPER.writeValueAsString(step))
                    .replace("{plan}", OBJECT_MAPPER.writeValueAsString(plan));

            // 2. 调用 invokeLlm
            AssistantMessage assistantMsg = invokeLlm(query, "json_object", "none", emitter);
            if (assistantMsg == null) return;

            // 3. 记录日志并解析 JSON
            String content = assistantMsg.getText();
            logger.info("PlannerAgent 生成消息: {}...",
                    content != null && content.length() > 100 ? content.substring(0, 100) : content);

            Plan updatedPlan = parseJsonToObject(content, Plan.class);
            if (updatedPlan == null) return;

            // 4. 拷贝更新规划中的 steps，避免数据污染
            List<Step> newSteps = new ArrayList<>();
            if (updatedPlan.getSteps() != null) {
                for (Step s : updatedPlan.getSteps()) {
                    newSteps.add(OBJECT_MAPPER.convertValue(s, Step.class));
                }
            }

            // 5. 查询旧规划中第一个未完成的规划
            int firstPendingIndex = -1;
            for (int idx = 0; idx < plan.getSteps().size(); idx++) {
                if (!plan.getSteps().get(idx).isDone()) {
                    firstPendingIndex = idx;
                    break;
                }
            }

            // 6. 如果有未完成步骤，则保留历史已完成步骤，追加新规划的步骤
            if (firstPendingIndex >= 0) {
                List<Step> updatedSteps = new ArrayList<>(plan.getSteps().subList(0, firstPendingIndex));
                updatedSteps.addAll(newSteps);
                plan.setSteps(updatedSteps);
            }

            emitter.accept(PlanEvent.builder()
                    .plan(toPlanData(plan))
                    .status(PlanEvent.PlanEventStatus.UPDATED)
                    .build());

        } catch (Exception e) {
            logger.error("更新规划失败: {}", e.getMessage(), e);
            emitter.accept(ErrorEvent.builder().error("更新规划失败: " + e.getMessage()).build());
        }
    }

    // ===================== 工具方法 =====================

    /**
     * 将 Plan 领域模型转换为 manus-common 的 PlanEvent.PlanData（用于 SSE 传输）
     */
    private PlanEvent.PlanData toPlanData(Plan plan) {
        List<PlanEvent.StepData> stepDataList = new ArrayList<>();
        if (plan.getSteps() != null) {
            for (Step s : plan.getSteps()) {
                stepDataList.add(PlanEvent.StepData.builder()
                        .id(s.getId())
                        .description(s.getDescription())
                        .status(s.getStatus() != null ? s.getStatus().name().toLowerCase() : "pending")
                        .result(s.getResult())
                        .success(s.getSuccess())
                        .build());
            }
        }
        return PlanEvent.PlanData.builder()
                .id(plan.getId())
                .title(plan.getTitle() != null ? plan.getTitle() : "")
                .goal(plan.getGoal() != null ? plan.getGoal() : "")
                .language(plan.getLanguage() != null ? plan.getLanguage() : "")
                .message(plan.getMessage() != null ? plan.getMessage() : "")
                .steps(stepDataList)
                .status(plan.getStatus() != null ? plan.getStatus().name().toLowerCase() : "pending")
                .build();
    }

    /**
     * 解析 JSON 字符串为指定类型对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @return 解析结果，失败返回 null
     */
    private <T> T parseJsonToObject(String json, Class<T> clazz) {
        try {
            if (json == null || json.isBlank()) return null;
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("JSON 解析失败: {}, content: {}", e.getMessage(),
                    json != null && json.length() > 200 ? json.substring(0, 200) : json);
            return null;
        }
    }
}

