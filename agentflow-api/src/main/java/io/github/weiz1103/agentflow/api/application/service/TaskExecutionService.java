package io.github.weiz1103.agentflow.api.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.weiz1103.agentflow.api.domain.model.task.TaskExecution;
import io.github.weiz1103.agentflow.api.domain.model.task.TaskExecutionStatus;
import io.github.weiz1103.agentflow.api.domain.repository.TaskExecutionRepository;
import io.github.weiz1103.agentflow.api.infrastructure.observability.ExecutionSnapshot;
import io.github.weiz1103.agentflow.common.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 任务执行生命周期服务。
 */
@Service
public class TaskExecutionService {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutionService.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private final TaskExecutionRepository repository;

    public TaskExecutionService(TaskExecutionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TaskExecution registerSubmitted(String sessionId, String taskId, String outputStreamKey) {
        TaskExecution execution = repository.findByTaskId(taskId)
                .orElse(TaskExecution.submitted(sessionId, taskId, outputStreamKey));
        execution.setSessionId(sessionId);
        execution.setOutputStreamKey(outputStreamKey);
        if (execution.getSubmittedAt() == null) {
            execution.setSubmittedAt(LocalDateTime.now());
        }
        if (execution.getStatus() == null) {
            execution.setStatus(TaskExecutionStatus.SUBMITTED);
        }
        execution.setUpdatedAt(LocalDateTime.now());
        repository.save(execution);
        return execution;
    }

    @Transactional
    public void markDispatched(String taskId, String dispatchMessageId, String consumerGroup) {
        repository.findByTaskId(taskId).ifPresent(execution -> {
            execution.markDispatched(dispatchMessageId, consumerGroup);
            repository.save(execution);
        });
    }

    @Transactional
    public void markRunning(String taskId, String consumerName, String workerId, Duration leaseDuration) {
        repository.findByTaskId(taskId).ifPresent(execution -> {
            execution.markRunning(consumerName, workerId, leaseDuration);
            repository.save(execution);
        });
    }

    @Transactional
    public void heartbeat(String taskId, String workerId, Duration leaseDuration) {
        repository.findByTaskId(taskId).ifPresent(execution -> {
            execution.setWorkerId(workerId);
            execution.extendLease(leaseDuration);
            execution.setUpdatedAt(LocalDateTime.now());
            repository.save(execution);
        });
    }

    @Transactional
    public Optional<TaskExecution> requestCancelBySession(String sessionId, String reason) {
        Optional<TaskExecution> executionOpt = repository.findLatestBySessionId(sessionId);
        executionOpt.ifPresent(execution -> {
            execution.requestCancel(reason);
            repository.save(execution);
        });
        return executionOpt;
    }

    @Transactional
    public void markCancelled(String taskId, String reason) {
        repository.findByTaskId(taskId).ifPresent(execution -> {
            execution.markCancelled(reason);
            repository.save(execution);
        });
    }

    @Transactional
    public void markFinished(String taskId) {
        repository.findByTaskId(taskId).ifPresent(execution -> {
            execution.markCompleted();
            repository.save(execution);
        });
    }

    @Transactional
    public void markFailed(String taskId, String errorMessage) {
        repository.findByTaskId(taskId).ifPresent(execution -> {
            execution.markFailed(errorMessage);
            repository.save(execution);
        });
    }

    @Transactional
    public void markWaiting(String taskId) {
        repository.findByTaskId(taskId).ifPresent(execution -> {
            execution.markWaiting();
            repository.save(execution);
        });
    }

    @Transactional(readOnly = true)
    public boolean isCancelRequested(String taskId) {
        return repository.findByTaskId(taskId)
                .map(TaskExecution::isCancelRequested)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<TaskExecution> findByTaskId(String taskId) {
        return repository.findByTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public Optional<TaskExecution> findLatestBySessionId(String sessionId) {
        return repository.findLatestBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public List<TaskExecution> findBySessionId(String sessionId) {
        return repository.findBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public List<TaskExecution> findLoopDetected(int limit) {
        return repository.findLoopDetected(limit);
    }

    @Transactional(readOnly = true)
    public long countSubmittedSince(String sessionId, LocalDateTime cutoff) {
        return repository.countSubmittedSince(sessionId, cutoff);
    }

    @Transactional(readOnly = true)
    public long countActiveBySessionId(String sessionId) {
        return repository.countActiveBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findReplayEvents(String taskId) {
        return repository.findByTaskId(taskId)
                .map(TaskExecution::getReplayEvents)
                .filter(events -> !events.isEmpty())
                .orElse(List.of());
    }

    @Transactional
    public void syncObservation(String taskId, ExecutionSnapshot snapshot, BaseEvent event) {
        TaskExecution execution = repository.findByTaskId(taskId)
                .orElseGet(() -> TaskExecution.submitted(snapshot.sessionId(), taskId, "task_output_" + taskId));

        execution.setEventCount(snapshot.eventCount());
        execution.setToolCallCount(snapshot.toolCallCount());
        execution.setStepStartCount(snapshot.stepStartCount());
        execution.setRepeatedToolCallCount(snapshot.repeatedToolCallCount());
        execution.setLoopDetected(snapshot.loopDetected());
        execution.setLastToolFingerprint(snapshot.lastToolFingerprint());
        execution.setTerminalEvent(snapshot.terminalEvent());
        execution.setErrorMessage(snapshot.errorMessage());
        execution.setSubmittedAt(snapshot.submittedAt() != null ? snapshot.submittedAt() : execution.getSubmittedAt());
        execution.setStartedAt(snapshot.startedAt() != null ? snapshot.startedAt() : execution.getStartedAt());
        execution.setWarningsSafe(snapshot.warnings());
        if (snapshot.finishedAt() != null) {
            execution.setFinishedAt(snapshot.finishedAt());
        }
        if (event != null) {
            Map<String, Object> eventPayload = toEventMap(event);
            execution.setLastEventId(event.getId());
            execution.appendReplayEvent(eventPayload);
        }

        switch (snapshot.status()) {
            case "waiting" -> execution.markWaiting();
            case "completed" -> execution.markCompleted();
            case "failed" -> execution.markFailed(snapshot.errorMessage());
            case "cancelled" -> execution.markCancelled(execution.getCancelReason() != null ? execution.getCancelReason() : "cancelled");
            default -> execution.setUpdatedAt(LocalDateTime.now());
        }
        repository.save(execution);
    }

    private Map<String, Object> toEventMap(BaseEvent event) {
        try {
            return JSON.convertValue(event, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            log.warn("事件转换为回放记录失。 eventType={}, error={}", event.getType(), e.getMessage());
            return Map.of("type", event.getType(), "id", event.getId());
        }
    }
}


