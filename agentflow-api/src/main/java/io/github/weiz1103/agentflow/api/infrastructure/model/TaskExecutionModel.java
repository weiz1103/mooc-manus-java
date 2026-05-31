package io.github.weiz1103.agentflow.api.infrastructure.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 持久化任务执行记录。
 */
@Entity
@Table(name = "task_executions", indexes = {
        @Index(name = "idx_task_execution_session_submitted", columnList = "session_id, submitted_at"),
        @Index(name = "idx_task_execution_status", columnList = "status"),
        @Index(name = "idx_task_execution_loop", columnList = "loop_detected")
})
public class TaskExecutionModel {

    @Id
    @Column(name = "task_id", length = 255)
    private String taskId;

    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    @Column(name = "output_stream_key", length = 255)
    private String outputStreamKey;

    @Column(name = "dispatch_message_id", length = 255)
    private String dispatchMessageId;

    @Column(name = "consumer_group", length = 255)
    private String consumerGroup;

    @Column(name = "consumer_name", length = 255)
    private String consumerName;

    @Column(name = "worker_id", length = 255)
    private String workerId;

    @Column(name = "status", length = 64)
    private String status;

    @Column(name = "cancel_requested")
    private boolean cancelRequested;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "lease_expires_at")
    private LocalDateTime leaseExpiresAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_event_id", length = 255)
    private String lastEventId;

    @Column(name = "terminal_event", length = 64)
    private String terminalEvent;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "event_count")
    private long eventCount;

    @Column(name = "tool_call_count")
    private long toolCallCount;

    @Column(name = "step_start_count")
    private long stepStartCount;

    @Column(name = "repeated_tool_call_count")
    private long repeatedToolCallCount;

    @Column(name = "loop_detected")
    private boolean loopDetected;

    @Column(name = "last_tool_fingerprint", columnDefinition = "TEXT")
    private String lastToolFingerprint;

    @Column(name = "warnings", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<String> warnings;

    @Column(name = "replay_events", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<Map<String, Object>> replayEvents;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getOutputStreamKey() { return outputStreamKey; }
    public void setOutputStreamKey(String outputStreamKey) { this.outputStreamKey = outputStreamKey; }
    public String getDispatchMessageId() { return dispatchMessageId; }
    public void setDispatchMessageId(String dispatchMessageId) { this.dispatchMessageId = dispatchMessageId; }
    public String getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(String consumerGroup) { this.consumerGroup = consumerGroup; }
    public String getConsumerName() { return consumerName; }
    public void setConsumerName(String consumerName) { this.consumerName = consumerName; }
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isCancelRequested() { return cancelRequested; }
    public void setCancelRequested(boolean cancelRequested) { this.cancelRequested = cancelRequested; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getLeaseExpiresAt() { return leaseExpiresAt; }
    public void setLeaseExpiresAt(LocalDateTime leaseExpiresAt) { this.leaseExpiresAt = leaseExpiresAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getLastEventId() { return lastEventId; }
    public void setLastEventId(String lastEventId) { this.lastEventId = lastEventId; }
    public String getTerminalEvent() { return terminalEvent; }
    public void setTerminalEvent(String terminalEvent) { this.terminalEvent = terminalEvent; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public long getEventCount() { return eventCount; }
    public void setEventCount(long eventCount) { this.eventCount = eventCount; }
    public long getToolCallCount() { return toolCallCount; }
    public void setToolCallCount(long toolCallCount) { this.toolCallCount = toolCallCount; }
    public long getStepStartCount() { return stepStartCount; }
    public void setStepStartCount(long stepStartCount) { this.stepStartCount = stepStartCount; }
    public long getRepeatedToolCallCount() { return repeatedToolCallCount; }
    public void setRepeatedToolCallCount(long repeatedToolCallCount) { this.repeatedToolCallCount = repeatedToolCallCount; }
    public boolean isLoopDetected() { return loopDetected; }
    public void setLoopDetected(boolean loopDetected) { this.loopDetected = loopDetected; }
    public String getLastToolFingerprint() { return lastToolFingerprint; }
    public void setLastToolFingerprint(String lastToolFingerprint) { this.lastToolFingerprint = lastToolFingerprint; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public List<Map<String, Object>> getReplayEvents() { return replayEvents; }
    public void setReplayEvents(List<Map<String, Object>> replayEvents) { this.replayEvents = replayEvents; }
}


