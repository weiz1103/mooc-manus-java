package com.imooc.manus.api.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.application.agent.AgentRunner;
import com.imooc.manus.api.domain.external.Task;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.external.TaskDispatchQueue;
import com.imooc.manus.api.domain.model.task.AgentTaskCommand;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.infrastructure.event.AgentEventBus;
import com.imooc.manus.api.domain.exception.SessionBusyException;
import com.imooc.manus.api.domain.exception.SessionNotFoundException;
import com.imooc.manus.api.infrastructure.config.AppProperties;
import com.imooc.manus.api.infrastructure.external.task.RedisStreamTask;
import com.imooc.manus.api.interfaces.sse.SsePublisher;
import com.imooc.manus.api.infrastructure.observability.ExecutionObservationSink;
import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.common.event.DoneEvent;
import com.imooc.manus.common.event.ErrorEvent;
import com.imooc.manus.common.event.MessageEvent;
import com.imooc.manus.common.event.WaitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天业务服务。
 *
 * <p>主流程编排中心，对应前端的两种聊天场景：
 * <ul>
 *   <li><b>发送新消息</b>：{@link #sendMessage} — 校验 → 记录 → 启动 Agent 执行</li>
 *   <li><b>断线续流</b>：{@link #resumeStream} — 从 Redis Stream 重放未推送事件</li>
 * </ul>
 * </p>
 *
 * <p>本类只负责流程编排，不涉及 AI 推理细节（由 {@link AgentRunner} 负责）
 * 和事件副作用（由 {@link AgentEventBus} 负责）。</p>
 */
@Service
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 07:47:17
 */
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final ObjectMapper JSON = new ObjectMapper()
            .findAndRegisterModules()
            .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final SessionRepository sessionRepository;
    private final AgentTaskService agentTaskService;
    private final AgentRunner agentRunner;
    private final AgentEventBus eventBus;
    private final TaskDispatchQueue taskDispatchQueue;
    private final TaskExecutionService taskExecutionService;
    private final ExecutionAdmissionService executionAdmissionService;
    private final ExecutionObservationSink observationSink;
    private final AppProperties appProperties;

    public ChatService(SessionRepository sessionRepository,
                       AgentTaskService agentTaskService,
                       AgentRunner agentRunner,
                       AgentEventBus eventBus,
                       TaskDispatchQueue taskDispatchQueue,
                       TaskExecutionService taskExecutionService,
                       ExecutionAdmissionService executionAdmissionService,
                       ExecutionObservationSink observationSink,
                       AppProperties appProperties) {
        this.sessionRepository = sessionRepository;
        this.agentTaskService = agentTaskService;
        this.agentRunner = agentRunner;
        this.eventBus = eventBus;
        this.taskDispatchQueue = taskDispatchQueue;
        this.taskExecutionService = taskExecutionService;
        this.executionAdmissionService = executionAdmissionService;
        this.observationSink = observationSink;
        this.appProperties = appProperties;
    }

    /**
     * 处理一次聊天请求。
     *
     * <p>根据是否携带消息体，自动路由到发送新消息或断线续流两条路径。
     * 此方法在虚拟线程中执行（由 Controller 启动），可安全阻塞。</p>
     *
     * @param sessionId   会话 ID
     * @param message     用户消息（空/null 表示断线续流）
     * @param attachments 附件文件路径
     * @param eventId     客户端最后收到的事件 ID（用于续流断点）
     * @param timestamp   消息时间戳（前端传入，用于记录）
     * @param publisher   SSE 推送器
     */
    public void chat(String sessionId, String message, List<String> attachments,
                     String eventId, LocalDateTime timestamp, SsePublisher publisher) {
        if (StringUtils.hasText(message)) {
            sendMessage(sessionId, message, attachments, timestamp, publisher);
        } else {
            resumeStream(sessionId, eventId, publisher);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 发送新消息
    // ─────────────────────────────────────────────────────────────────────────

    public void sendMessage(String sessionId, String message, List<String> attachments,
                            LocalDateTime timestamp, SsePublisher publisher) {
        // 1. 加载会话，校验状态
        Session session = sessionRepository.getById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (session.isRunning()) {
            throw new SessionBusyException(sessionId);
        }

        java.util.Optional<String> rejectReason = executionAdmissionService.rejectReason(sessionId);
        if (rejectReason.isPresent()) {
            publishAdmissionDenied(publisher, rejectReason.get());
            return;
        }

        // 2. 记录用户消息（更新 latest_message 字段）
        LocalDateTime msgTime = timestamp != null ? timestamp : LocalDateTime.now();
        sessionRepository.updateLatestMessage(sessionId, message, msgTime);

        // 3. 确保 Task 和 Sandbox 就绪，并将会话状态置为 RUNNING
        Task task = agentTaskService.ensureTask(session);
        session.start();
        sessionRepository.updateStatus(sessionId, session.getStatus());

        // 4. 发布用户消息事件（让前端看到自己发出的消息回显）
        MessageEvent userEvent = MessageEvent.builder()
                .role("user")
                .message(message)
                .attachments(resolveAttachmentPaths(attachments))
                .build();
        // 用户消息也写入 Task Stream，支持续流时回放
        recordToStream(task, userEvent);
        eventBus.dispatch(session, userEvent, publisher);

        if (!appProperties.getAgent().isAsyncExecutionEnabled()) {
            // 5A. 同步执行回退路径（保留兼容开关）
            log.info("开始同步 Agent 执行: sessionId={}", sessionId);
            taskExecutionService.registerSubmitted(sessionId, task.getId(), RedisStreamTask.outputStreamKey(task.getId()));
            observationSink.onTaskSubmitted(sessionId, task.getId());
            observationSink.onTaskStarted(sessionId, task.getId());
            agentRunner.run(session, task, message, resolveAttachmentPaths(attachments), publisher);
            observationSink.onTaskFinished(sessionId, task.getId());
            return;
        }

        // 5B. 异步任务提交路径：提交命令给后台 worker，本请求只负责 tail 输出流
        AgentTaskCommand command = new AgentTaskCommand(
                sessionId,
                task.getId(),
                message,
                resolveAttachmentPaths(attachments),
                msgTime
        );
        taskExecutionService.registerSubmitted(sessionId, task.getId(), RedisStreamTask.outputStreamKey(task.getId()));
        observationSink.onTaskSubmitted(sessionId, task.getId());
        String dispatchMessageId = taskDispatchQueue.submit(command);
        taskExecutionService.markDispatched(task.getId(), dispatchMessageId, taskDispatchQueue.getConsumerGroup());
        log.info("已提交后台 Agent 任务: sessionId={}, taskId={}, dispatchMessageId={}",
                sessionId, task.getId(), dispatchMessageId);

        // 6. 当前连接改为从用户消息之后继续消费该 task 的输出事件，实现“提交 + 结果回推”
        replayFromStream(session, task, userEvent.getId(), publisher);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 断线续流
    // ─────────────────────────────────────────────────────────────────────────

    public void resumeStream(String sessionId, String lastEventId, SsePublisher publisher) {
        Session session = sessionRepository.getById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        Task task = agentTaskService.findTask(session).orElse(null);
        if (task == null) {
            log.info("会话无进行中任务，续流直接返回: sessionId={}", sessionId);
            return;
        }

        log.info("开始断线续流: sessionId={}, lastEventId={}", sessionId, lastEventId);
        replayFromStream(session, task, lastEventId, publisher);
    }

    /**
     * 从 Redis Stream 重放事件，直到遇到终止事件或流尽。
     *
     * <p>续流分两阶段：
     * <ol>
     *   <li>会话 RUNNING 中：阻塞等待新事件（最多等待 5 分钟）</li>
     *   <li>会话已结束：非阻塞读取剩余事件后退出</li>
     * </ol>
     * </p>
     */
    private void replayFromStream(Session session, Task task, String lastEventId, SsePublisher publisher) {
        String nextId = StringUtils.hasText(lastEventId) ? lastEventId : "0";
        boolean waitForNew = session.isRunning();
        int idleCount = 0;

        // 阶段一：会话执行中，阻塞等待新事件
        while (waitForNew && !publisher.isClientGone()) {
            String[] record = task.getOutputStream().get(nextId, 1000); // 1秒超时
            if (record == null) {
                if (++idleCount >= 300) { // 最多等待 5 分钟
                    log.info("续流等待超时，退出: sessionId={}", session.getId());
                    break;
                }
                continue;
            }
            idleCount = 0;
            BaseEvent event = deserialize(record);
            if (event != null) {
                nextId = record[0];
                event.setId(record[0]);
                publisher.publish(event);
                if (isTerminal(event)) return;
            } else {
                nextId = record[0];
                log.warn("续流反序列化失败，跳过异常事件: id={}", nextId);
            }
        }

        // 阶段二：非阻塞读取剩余事件
        while (!publisher.isClientGone()) {
            String[] record = task.getOutputStream().get(nextId, 0);
            if (record == null) break;
            BaseEvent event = deserialize(record);
            if (event != null) {
                nextId = record[0];
                event.setId(record[0]);
                publisher.publish(event);
                if (isTerminal(event)) break;
            } else {
                nextId = record[0];
                log.warn("续流反序列化失败，跳过异常事件: id={}", nextId);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 私有工具方法
    // ─────────────────────────────────────────────────────────────────────────

    private void recordToStream(Task task, BaseEvent event) {
        if (task == null) return;
        try {
            String id = task.getOutputStream().put(JSON.writeValueAsString(event));
            event.setId(id);
        } catch (Exception e) {
            log.debug("事件写入 Stream 失败: {}", e.getMessage());
        }
    }

    private BaseEvent deserialize(String[] record) {
        if (record == null || record.length < 2) return null;
        try {
            return JSON.readValue(record[1], BaseEvent.class);
        } catch (Exception e) {
            log.error("反序列化事件失败: {}, 原始数据: {}", e.getMessage(), record[1]);
            return null;
        }
    }

    private boolean isTerminal(BaseEvent event) {
        return event instanceof DoneEvent || event instanceof ErrorEvent || event instanceof WaitEvent;
    }

    private void publishAdmissionDenied(SsePublisher publisher, String reason) {
        publisher.publish(ErrorEvent.builder().error(resolveAdmissionMessage(reason)).build());
        publisher.publish(DoneEvent.builder().build());
    }

    private String resolveAdmissionMessage(String reason) {
        return switch (reason) {
            case "session_rate_limit_exceeded" -> "当前会话触发发送频率限制，请稍后再试";
            case "session_active_task_limit_exceeded" -> "当前会话已有进行中的任务，请等待当前任务结束后再试";
            case "session_daily_task_quota_exceeded" -> "当前会话今日任务配额已用尽，请明日再试或调整配额";
            default -> "当前请求未通过系统准入校验";
        };
    }

    private List<String> resolveAttachmentPaths(List<String> attachments) {
        return attachments != null ? attachments : List.of();
    }
}
