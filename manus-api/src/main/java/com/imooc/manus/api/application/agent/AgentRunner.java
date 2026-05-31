package com.imooc.manus.api.application.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.domain.external.Task;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.model.session.SessionStatus;
import com.imooc.manus.api.domain.service.agent.AgentStrategy;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.infrastructure.event.AgentEventBus;
import com.imooc.manus.api.interfaces.sse.SsePublisher;
import com.imooc.manus.api.infrastructure.observability.ExecutionObservationSink;
import com.imooc.manus.api.application.service.TaskExecutionService;
import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.common.event.DoneEvent;
import com.imooc.manus.common.event.ErrorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent 执行引擎。
 *
 * <p>驱动 AI 策略（{@link AgentStrategy}）执行，消费产出的事件流，
 * 并通过 {@link AgentEventBus} 完成事件的持久化、状态同步和 SSE 推送。</p>
 *
 * <p>异常兜底：无论 AI 执行成功与否，都保证会话最终从 RUNNING 状态退出，
 * 防止会话永久锁死在执行中。</p>
 */
@Component
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 17:11:26
 */
public class AgentRunner {

    private static final Logger log = LoggerFactory.getLogger(AgentRunner.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private final AgentStrategy agentStrategy;
    private final AgentEventBus eventBus;
    private final SessionRepository sessionRepository;
    private final ExecutionObservationSink observationSink;
    private final TaskExecutionService taskExecutionService;

    public AgentRunner(AgentStrategy agentStrategy,
                       AgentEventBus eventBus,
                       SessionRepository sessionRepository,
                       ExecutionObservationSink observationSink,
                       TaskExecutionService taskExecutionService) {
        this.agentStrategy = agentStrategy;
        this.eventBus = eventBus;
        this.sessionRepository = sessionRepository;
        this.observationSink = observationSink;
        this.taskExecutionService = taskExecutionService;
    }

    /**
     * 执行一轮 Agent 对话。
     *
     * @param session     当前会话
     * @param task        Redis Stream 任务管道（用于断线续流）
     * @param message     用户输入
     * @param attachments 附件文件路径列表
     * @param publisher   SSE 推送器
     */
    public void run(Session session, Task task,
                    String message, List<String> attachments,
                    SsePublisher publisher) {
        String taskId = task != null ? task.getId() : session.getTaskId();
        try {
            checkCancelled(taskId);
            agentStrategy.execute(session.getId(), message, attachments)
                    .doOnNext(event -> {
                        checkCancelled(taskId);
                        // 将事件写入 Redis Stream（支持客户端断线后重连续流）
                        recordToTaskStream(task, event);
                        if (taskId != null) {
                            observationSink.onEvent(session.getId(), taskId, event);
                            taskExecutionService.heartbeat(taskId, null, java.time.Duration.ofSeconds(30));
                        }
                        // 触发事件总线：持久化 → 状态同步 → SSE 推送
                        eventBus.dispatch(session, event, publisher);
                    })
                    .blockLast();

            log.info("Agent 执行完成: sessionId={}", session.getId());

        } catch (TaskCancelledException e) {
            log.info("Agent 执行被取消: sessionId={}, reason={}", session.getId(), e.getMessage());
            DoneEvent doneEvent = DoneEvent.builder().build();
            recordToTaskStream(task, doneEvent);
            if (taskId != null) {
                observationSink.onEvent(session.getId(), taskId, doneEvent);
                observationSink.onTaskCancelled(session.getId(), taskId, e.getMessage());
                taskExecutionService.markCancelled(taskId, e.getMessage());
            }
            eventBus.dispatch(session, doneEvent, publisher);
            sessionRepository.updateStatus(session.getId(), SessionStatus.COMPLETED);

        } catch (Exception e) {
            log.error("Agent 执行异常: sessionId={}, error={}", session.getId(), e.getMessage(), e);

            // 发送错误事件通知前端
            ErrorEvent errorEvent = ErrorEvent.builder().error(e.getMessage()).build();
            if (taskId != null) {
                observationSink.onEvent(session.getId(), taskId, errorEvent);
                observationSink.onTaskFailed(session.getId(), taskId, e.getMessage());
            }
            eventBus.dispatch(session, errorEvent, publisher);

            // 兜底：将会话状态从 RUNNING 归位，防止永久锁死
            sessionRepository.updateStatus(session.getId(), SessionStatus.COMPLETED);
            if (taskId != null) {
                taskExecutionService.markFailed(taskId, e.getMessage());
            }
        }
    }

    private void checkCancelled(String taskId) {
        if (taskId != null && taskExecutionService.isCancelRequested(taskId)) {
            throw new TaskCancelledException("task_cancel_requested");
        }
    }

    private void recordToTaskStream(Task task, BaseEvent event) {
        if (task == null) return;
        try {
            String json = JSON.writeValueAsString(event);
            String streamId = task.getOutputStream().put(json);
            // 用 Stream ID 作为事件 ID，支持精确断点续流
            event.setId(streamId);
        } catch (Exception e) {
            log.debug("事件写入 Task Stream 失败: {}", e.getMessage());
        }
    }
}
