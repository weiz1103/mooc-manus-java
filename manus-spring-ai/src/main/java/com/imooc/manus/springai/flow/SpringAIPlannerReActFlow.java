package com.imooc.manus.springai.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.common.event.*;
import com.imooc.manus.springai.agent.SpringAIPlannerAgent;
import com.imooc.manus.springai.agent.SpringAIReActAgent;
import com.imooc.manus.springai.config.SpringAIFlowConfig;
import com.imooc.manus.springai.memory.AgentMemoryStore;
import com.imooc.manus.springai.model.AgentMessage;
import com.imooc.manus.springai.model.ExecutionStatus;
import com.imooc.manus.springai.model.Plan;
import com.imooc.manus.springai.model.Step;
import com.imooc.manus.springai.session.SessionStateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

/**
 * Spring AI 规划与执行流（PlannerReAct Flow）。
 *
 * <p>
 * 多Agent系统/flow = PlannerAgent + ReActAgent
 * 顺序：
 * 1. PlannerAgent 生成规划；
 * 2. 循环取出规划中的子步骤，让 ReActAgent 执行，依次迭代；
 * 3. ReActAgent 执行完每一个子步骤之后，需要将子步骤结果+Plan传递给PlannerAgent让其更新规划；
 * 4. 循环取出规划中的子步骤，让 ReActAgent 执行，依次迭代；
 * 5. ...
 * 6. 直到所有子任务/步骤都完成，这时候将子步骤的所有结果汇总进行总结(ReActAgent)；
 * </p>
 *
 * <p><b>扩展点：</b>
 * - 可替换 AgentMemoryStore 实现（如 DB 持久化）
 * - 可替换 SessionStateLoader 实现
 * - 可添加自定义 ToolCallback 列表
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-28 13:59:38
 */
public class SpringAIPlannerReActFlow {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIPlannerReActFlow.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private final ChatClient chatClient;
    private final List<ToolCallback> toolCallbacks;
    private final AgentMemoryStore memoryStore;
    private final SessionStateLoader sessionStateLoader;
    private final SpringAIFlowConfig config;

    /**
     * 构造函数，完成规划与执行流的初始化。
     *
     * @param chatClient          Spring AI ChatClient（已配置好 model 等参数）
     * @param toolCallbacks       工具回调列表（FileTool、ShellTool、BrowserTool 等）
     * @param memoryStore         记忆持久化存储
     * @param sessionStateLoader  会话状态加载/更新接口
     * @param config              流程配置（maxRetries、maxIterations 等）
     */
    public SpringAIPlannerReActFlow(
            ChatClient chatClient,
            List<ToolCallback> toolCallbacks,
            AgentMemoryStore memoryStore,
            SessionStateLoader sessionStateLoader,
            SpringAIFlowConfig config
    ) {
        this.chatClient = chatClient;
        this.toolCallbacks = toolCallbacks;
        this.memoryStore = memoryStore;
        this.sessionStateLoader = sessionStateLoader;
        this.config = config;
    }

    /**
     * 传递消息，运行流，在六步调用 planner&react 智能体组合完成任务并返回对应事件。
     *
     * <p>返回 Flux&lt;BaseEvent&gt; 以支持响应式流式处理，内部同步执行（通过 Flux.create 包装）。</p>
     *
     * @param sessionId    会话id
     * @param message      用户消息文本
     * @param attachPaths  附件文件路径列表
     * @return 事件流（Flux&lt;BaseEvent&gt;）
     */
    public Flux<BaseEvent> invoke(String sessionId, String message, List<String> attachPaths) {
        return Flux.create(sink -> {
            Consumer<BaseEvent> emitter = event -> {
                if (!sink.isCancelled()) {
                    sink.next(event);
                }
            };
            try {
                runFlow(sessionId, message, attachPaths, emitter);
            } catch (Exception e) {
                logger.error("PlannerReActFlow 执行异常，会话[{}]: {}", sessionId, e.getMessage(), e);
                sink.next(ErrorEvent.builder().error(e.getMessage()).build());
            } finally {
                sink.complete();
            }
        });
    }

    /**
     * 核心流程执行方法（同步阻塞）。
     *
     * @param sessionId    会话id
     * @param message      用户消息文本
     * @param attachPaths  附件文件路径列表
     * @param emitter      事件发射器
     */
    private void runFlow(String sessionId, String message, List<String> attachPaths, Consumer<BaseEvent> emitter) {
        String safeMessage = message != null ? message : "";

        String sessionStatus = sessionStateLoader.getSessionStatus(sessionId);

        // 2. 判断会话的状态是不是空闲
        //    如果不是则有可能有两种状态:
        //     - 任务未结束，还在运行，但是用户又传递了一条消息
        //     - Agent 在等待人类输入，这时候人类输入了
        //    这时候均需要处理历史消息列表，避免AI(工具调用消息)后直接接上人类消息
        AgentMessage msg = AgentMessage.of(safeMessage, attachPaths);
        if (!"PENDING".equals(sessionStatus)) {
            logger.debug("会话[{}]不处于空闲状态，回滚数据确保消息列表格式正确", sessionId);
            // 创建临时 Agent 对象进行回滚（避免重复代码）
            createPlannerAgent(sessionId).rollBack(msg);
            createReActAgent(sessionId).rollBack(msg);
        }

        // 3. 根据会话当前状态确定流的初始状态
        FlowStatus flowStatus;
        if ("RUNNING".equals(sessionStatus)) {
            // 会话处于运行中状态并传递了新消息 → 需要重新规划
            logger.debug("会话[{}]处于运行状态并传递了新消息", sessionId);
            flowStatus = FlowStatus.PLANNING;
        } else if ("WAITING".equals(sessionStatus)) {
            // 会话处于等待人类输入，这时候人类输入了 → 直接执行
            logger.debug("会话[{}]处于等待状态并传递了新消息", sessionId);
            flowStatus = FlowStatus.EXECUTING;
        } else {
            flowStatus = FlowStatus.IDLE;
        }

        sessionStateLoader.updateSessionStatus(sessionId, "RUNNING");

        // 5. 获取当前会话中最新事件（已有的 Plan）
        Plan plan = loadLatestPlan(sessionId);
        logger.info("Planner&ReAct流接收消息: {}...",
                safeMessage.length() > 50 ? safeMessage.substring(0, 50) : safeMessage);

        // 6. 定义当前正在执行的子步骤
        Step currentStep = null;

        // 7. 创建 Planner 和 React Agent 实例（per-session）
        SpringAIPlannerAgent plannerAgent = createPlannerAgent(sessionId);
        SpringAIReActAgent reactAgent = createReActAgent(sessionId);

        while (true) {
            // 9. 如果流的状态为空闲，则只需要将状态修改为规划中
            if (flowStatus == FlowStatus.IDLE) {
                logger.info("Planner&ReAct流状态从{}变成{}", FlowStatus.IDLE, FlowStatus.PLANNING);
                flowStatus = FlowStatus.PLANNING;
            } else if (flowStatus == FlowStatus.PLANNING) {
                // 10. 流状态为规划中，则调用规划 Agent
                logger.info("Planner&ReAct流开始创建规划/Plan");
                plan = plannerAgent.createPlan(msg, event -> {
                    // 11. 判断规划 Agent 是否返回规划事件
                    if (event instanceof PlanEvent planEvent && planEvent.getStatus() == PlanEvent.PlanEventStatus.CREATED) {
                        // 12. 创建规划成功时需要更新规划
                        logger.info("Planner&ReAct流成功创建规划, 共有: {} 步",
                                planEvent.getPlan() != null && planEvent.getPlan().getSteps() != null
                                        ? planEvent.getPlan().getSteps().size() : 0);

                        // 13. 在规划中同步生成了会话标题、初始AI消息
                        if (planEvent.getPlan() != null) {
                            emitter.accept(TitleEvent.builder().title(planEvent.getPlan().getTitle()).build());
                            emitter.accept(MessageEvent.builder()
                                    .role("assistant")
                                    .message(planEvent.getPlan().getMessage())
                                    .build());
                        }
                    }
                    // 14. 将生成的事件直接输出（一般来说是 PlanEvent）
                    emitter.accept(event);
                });

                // 15. 规划创建完成，更新流状态为执行中
                logger.info("Planner&ReAct流状态从{}变成{}", FlowStatus.PLANNING, FlowStatus.EXECUTING);
                flowStatus = FlowStatus.EXECUTING;

                // 16. 判断规划是否生成，步骤是否正常
                if (plan == null || plan.getSteps().isEmpty()) {
                    logger.info("Planner&ReAct流创建规划失败或无子步骤");
                    flowStatus = FlowStatus.COMPLETED;
                } else {
                    // 保存最新规划到会话
                    savePlan(sessionId, plan);
                }
            } else if (flowStatus == FlowStatus.EXECUTING) {
                // 17. 流的状态为执行中，先将规划状态调整为运行中，同时调用执行Agent完成每个子步骤
                if (plan != null) {
                    plan.setStatus(ExecutionStatus.RUNNING);
                }

                // 18. 获取当前规划的下一个需要执行的子步骤
                currentStep = plan != null ? plan.getNextStep().orElse(null) : null;

                // 19. 如果不存在下一个需要执行的子节，则更新流状态并执行后续步骤
                if (currentStep == null) {
                    logger.info("Planner&ReAct流状态从{}变成{}", FlowStatus.EXECUTING, FlowStatus.SUMMARIZING);
                    flowStatus = FlowStatus.SUMMARIZING;
                    continue;
                }

                // 20. 调用执行Agent执行对应的步骤
                logger.info("Planner&ReAct流开始执行步骤{}: {}...",
                        currentStep.getId(),
                        currentStep.getDescription() != null && currentStep.getDescription().length() > 50
                                ? currentStep.getDescription().substring(0, 50)
                                : currentStep.getDescription());

                boolean waitOccurred = reactAgent.executeStep(plan, currentStep, msg, emitter);

                if (waitOccurred) {
                    // 等待用户输入，提前返回
                    sessionStateLoader.updateSessionStatus(sessionId, "WAITING");
                    if (plan != null) savePlan(sessionId, plan);
                    return;
                }

                // 21. 压缩执行Agent记忆，避免上下文腐化+消耗大量Token
                logger.info("压缩{} Agent记忆/上下文", reactAgent.getName());
                reactAgent.compactMemory();

                // 22. 将状态更新为 updating
                flowStatus = FlowStatus.UPDATING;
                if (plan != null) savePlan(sessionId, plan);
            } else if (flowStatus == FlowStatus.UPDATING) {
                // 23. 流状态为更新表示需要更新规划
                logger.info("Planner&ReAct流开始更新规划");
                if (plan != null && currentStep != null) {
                    plannerAgent.updatePlan(plan, currentStep, emitter);
                }

                // 24. 规划更新完成，需要执行相应的子步骤
                logger.info("Planner&ReAct流状态从{}变成{}", FlowStatus.UPDATING, FlowStatus.EXECUTING);
                flowStatus = FlowStatus.EXECUTING;
            } else if (flowStatus == FlowStatus.SUMMARIZING) {
                // 25. 流状态为总结中，则意味着所有子步骤都执行完成
                logger.info("Planner&ReAct流开始总结");
                reactAgent.summarize(emitter);

                // 26. 总结完毕，意味着流即将结束
                logger.info("Planner&ReAct流状态从{}变成{}", FlowStatus.SUMMARIZING, FlowStatus.COMPLETED);
                flowStatus = FlowStatus.COMPLETED;
            } else if (flowStatus == FlowStatus.COMPLETED) {
                // 27. 规划状态已完成则更新 plan 状态，并发送规划事件通知API已完成
                if (plan != null) {
                    plan.setStatus(ExecutionStatus.COMPLETED);
                    savePlan(sessionId, plan);
                    emitter.accept(PlanEvent.builder()
                            .plan(toPlanData(plan))
                            .status(PlanEvent.PlanEventStatus.COMPLETED)
                            .build());
                }
                // 更新会话状态为已完成
                sessionStateLoader.updateSessionStatus(sessionId, "COMPLETED");
                break;
            }
        }

        emitter.accept(DoneEvent.builder().build());
        logger.info("Planner&ReAct流处理任务消息已完毕");
    }

    // ===================== 工厂方法（扩展点） =====================

    /**
     * 创建 PlannerAgent 实例（每次 invoke 调用时创建）。
     * <p><b>扩展点：</b>可通过重写此方法替换自定义 PlannerAgent 实现。</p>
     *
     * @param sessionId 会话id
     * @return SpringAIPlannerAgent 实例
     */
    protected SpringAIPlannerAgent createPlannerAgent(String sessionId) {
        return new SpringAIPlannerAgent(sessionId, memoryStore, chatClient, toolCallbacks, config);
    }

    /**
     * 创建 ReActAgent 实例（每次 invoke 调用时创建）。
     * <p><b>扩展点：</b>可通过重写此方法替换自定义 ReActAgent 实现。</p>
     *
     * @param sessionId 会话id
     * @return SpringAIReActAgent 实例
     */
    protected SpringAIReActAgent createReActAgent(String sessionId) {
        return new SpringAIReActAgent(sessionId, memoryStore, chatClient, toolCallbacks, config);
    }

    // ===================== 辅助方法 =====================

    /**
     * 从 SessionStateLoader 加载最新 Plan（JSON 反序列化）
     */
    private Plan loadLatestPlan(String sessionId) {
        String planJson = sessionStateLoader.getLatestPlanJson(sessionId);
        if (planJson == null || planJson.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(planJson, Plan.class);
        } catch (Exception e) {
            logger.warn("加载 Plan 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将 Plan 序列化后保存到 SessionStateLoader
     */
    private void savePlan(String sessionId, Plan plan) {
        try {
            sessionStateLoader.savePlanJson(sessionId, OBJECT_MAPPER.writeValueAsString(plan));
        } catch (Exception e) {
            logger.warn("保存 Plan 失败: {}", e.getMessage());
        }
    }

    /**
     * 将 Plan 转换为 manus-common PlanEvent.PlanData（用于 SSE 传输）
     */
    private PlanEvent.PlanData toPlanData(Plan plan) {
        List<PlanEvent.StepData> stepDataList = new java.util.ArrayList<>();
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
}

