package com.imooc.manus.api.infrastructure.task;

import com.imooc.manus.api.application.agent.AgentRunner;
import com.imooc.manus.api.domain.external.Task;
import com.imooc.manus.api.domain.external.TaskDispatchQueue;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.infrastructure.config.AppProperties;
import com.imooc.manus.api.infrastructure.observability.ExecutionObservationSink;
import com.imooc.manus.api.application.service.AgentTaskService;
import com.imooc.manus.api.application.service.TaskExecutionService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 后台 Agent 任务 Worker。
 *
 * <p>职责：从任务分发队列拉取一条 AgentTaskCommand，在后台线程中执行完整的
 * Agent 流程。这样 Controller 线程只负责提交与订阅输出，不再直接承载完整执行。</p>
 */
@Component
public class AgentTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskWorker.class);

    private final TaskDispatchQueue taskDispatchQueue;
    private final SessionRepository sessionRepository;
    private final AgentTaskService agentTaskService;
    private final AgentRunner agentRunner;
    private final ExecutionObservationSink observationSink;
    private final AppProperties appProperties;
    private final TaskExecutionService taskExecutionService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String workerId = "agent-worker-" + UUID.randomUUID();

    private Thread workerThread;

    public AgentTaskWorker(TaskDispatchQueue taskDispatchQueue,
                           SessionRepository sessionRepository,
                           AgentTaskService agentTaskService,
                           AgentRunner agentRunner,
                           ExecutionObservationSink observationSink,
                           AppProperties appProperties,
                           TaskExecutionService taskExecutionService) {
        this.taskDispatchQueue = taskDispatchQueue;
        this.sessionRepository = sessionRepository;
        this.agentTaskService = agentTaskService;
        this.agentRunner = agentRunner;
        this.observationSink = observationSink;
        this.appProperties = appProperties;
        this.taskExecutionService = taskExecutionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!appProperties.getAgent().isAsyncExecutionEnabled()) {
            log.info("异步 Agent Worker 未启用，继续使用同步执行模式");
            return;
        }
        if (running.compareAndSet(false, true)) {
            workerThread = Thread.ofVirtual().name("agent-task-worker").start(this::loop);
            log.info("后台 Agent Worker 已启动");
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    private void loop() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                for (TaskDispatchQueue.QueuedTask staleTask : taskDispatchQueue.claimStale(
                        workerId,
                        java.time.Duration.ofMillis(appProperties.getAgent().getDispatchClaimIdleMs()),
                        appProperties.getAgent().getDispatchClaimBatchSize())) {
                    handle(staleTask);
                }

                TaskDispatchQueue.QueuedTask queuedTask = taskDispatchQueue.poll(workerId, appProperties.getAgent().getWorkerPollIntervalMs());
                if (queuedTask == null) {
                    continue;
                }
                handle(queuedTask);
            } catch (Exception e) {
                log.error("后台 Agent Worker 主循环异常: {}", e.getMessage(), e);
                safeSleep(300L);
            }
        }
    }

    private void handle(TaskDispatchQueue.QueuedTask queuedTask) {
        var command = queuedTask.command();
        String sessionId = command.sessionId();
        String taskId = command.taskId();
        try {
            Session session = sessionRepository.getById(sessionId).orElse(null);
            if (session == null) {
                observationSink.onTaskFailed(sessionId, taskId, "session_not_found");
                taskDispatchQueue.ack(queuedTask.messageId());
                return;
            }

            if (!taskId.equals(session.getTaskId())) {
                observationSink.onTaskFailed(sessionId, taskId, "task_mismatch");
                taskDispatchQueue.ack(queuedTask.messageId());
                return;
            }

            Optional<Task> taskOpt = agentTaskService.findTask(session);
            if (taskOpt.isEmpty()) {
                observationSink.onTaskFailed(sessionId, taskId, "task_not_ready");
                taskDispatchQueue.ack(queuedTask.messageId());
                return;
            }

            if (taskExecutionService.isCancelRequested(taskId)) {
                observationSink.onTaskCancelled(sessionId, taskId, "task_cancel_requested_before_start");
                taskExecutionService.markCancelled(taskId, "task_cancel_requested_before_start");
                taskDispatchQueue.ack(queuedTask.messageId());
                return;
            }

            taskExecutionService.markDispatched(taskId, queuedTask.messageId(), taskDispatchQueue.getConsumerGroup());
            taskExecutionService.markRunning(taskId, workerId, workerId, java.time.Duration.ofMillis(appProperties.getAgent().getDispatchClaimIdleMs()));

            observationSink.onTaskStarted(sessionId, taskId);
            agentRunner.run(session, taskOpt.get(), command.message(), command.attachments(), null);
            observationSink.onTaskFinished(sessionId, taskId);
            taskDispatchQueue.ack(queuedTask.messageId());
        } catch (Exception e) {
            observationSink.onTaskFailed(sessionId, taskId, e.getMessage());
            taskExecutionService.markFailed(taskId, e.getMessage());
            log.error("后台执行 AgentTaskCommand 失败: sessionId={}, taskId={}, error={}", sessionId, taskId, e.getMessage(), e);
        }
    }

    private void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

