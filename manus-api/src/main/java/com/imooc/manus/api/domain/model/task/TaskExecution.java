package com.imooc.manus.api.domain.model.task;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 持久化的任务执行聚合。
 */
@Data
public class TaskExecution {

    private String taskId;
    private String sessionId;
    private String outputStreamKey;
    private String dispatchMessageId;
    private String consumerGroup;
    private String consumerName;
    private String workerId;
    private TaskExecutionStatus status;
    private boolean cancelRequested;
    private String cancelReason;
    private LocalDateTime leaseExpiresAt;
    private LocalDateTime submittedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime updatedAt;
    private String lastEventId;
    private String terminalEvent;
    private String errorMessage;
    private long eventCount;
    private long toolCallCount;
    private long stepStartCount;
    private long repeatedToolCallCount;
    private boolean loopDetected;
    private String lastToolFingerprint;
    private List<String> warnings;
    private List<Map<String, Object>> replayEvents;

    public static TaskExecution submitted(String sessionId, String taskId, String outputStreamKey) {
        TaskExecution execution = new TaskExecution();
        execution.taskId = taskId;
        execution.sessionId = sessionId;
        execution.outputStreamKey = outputStreamKey;
        execution.status = TaskExecutionStatus.SUBMITTED;
        execution.submittedAt = LocalDateTime.now();
        execution.updatedAt = execution.submittedAt;
        execution.warnings = new ArrayList<>();
        execution.replayEvents = new ArrayList<>();
        return execution;
    }

    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    public boolean isActive() {
        return status != null && status.isActive();
    }

    public void markDispatched(String messageId, String consumerGroup) {
        if (isTerminal()) {
            return;
        }
        this.dispatchMessageId = messageId;
        this.consumerGroup = consumerGroup;
        this.status = TaskExecutionStatus.DISPATCHED;
        touch();
    }

    public void markRunning(String consumerName, String workerId, Duration leaseDuration) {
        if (isTerminal()) {
            return;
        }
        this.consumerName = consumerName;
        this.workerId = workerId;
        this.status = TaskExecutionStatus.RUNNING;
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        extendLease(leaseDuration);
        touch();
    }

    public void markWaiting() {
        if (isTerminal()) {
            return;
        }
        this.status = TaskExecutionStatus.WAITING;
        touch();
    }

    public void markCompleted() {
        if (cancelRequested) {
            markCancelled(cancelReason != null ? cancelReason : "cancel_requested");
            return;
        }
        this.status = TaskExecutionStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
        touch();
    }

    public void markFailed(String errorMessage) {
        this.status = TaskExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.finishedAt = LocalDateTime.now();
        touch();
    }

    public void requestCancel(String reason) {
        if (isTerminal()) {
            return;
        }
        this.cancelRequested = true;
        this.cancelReason = reason;
        if (this.status != TaskExecutionStatus.CANCELLED) {
            this.status = TaskExecutionStatus.CANCEL_REQUESTED;
        }
        touch();
    }

    public void markCancelled(String reason) {
        this.cancelRequested = true;
        this.cancelReason = reason;
        this.status = TaskExecutionStatus.CANCELLED;
        this.finishedAt = LocalDateTime.now();
        touch();
    }

    public void extendLease(Duration leaseDuration) {
        if (leaseDuration == null || leaseDuration.isZero() || leaseDuration.isNegative()) {
            return;
        }
        this.leaseExpiresAt = LocalDateTime.now().plus(leaseDuration);
    }

    public void appendReplayEvent(Map<String, Object> eventPayload) {
        if (eventPayload == null) {
            return;
        }
        if (this.replayEvents == null) {
            this.replayEvents = new ArrayList<>();
        }
        this.replayEvents.add(eventPayload);
    }

    public void setWarningsSafe(List<String> warnings) {
        this.warnings = warnings == null ? new ArrayList<>() : new ArrayList<>(warnings);
    }

    public void setReplayEventsSafe(List<Map<String, Object>> replayEvents) {
        this.replayEvents = replayEvents == null ? new ArrayList<>() : new ArrayList<>(replayEvents);
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}

