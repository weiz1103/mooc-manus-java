package com.imooc.manus.api.infrastructure.repository;

import com.imooc.manus.api.domain.model.task.TaskExecution;
import com.imooc.manus.api.domain.model.task.TaskExecutionStatus;
import com.imooc.manus.api.domain.repository.TaskExecutionRepository;
import com.imooc.manus.api.infrastructure.model.TaskExecutionModel;
import com.imooc.manus.api.infrastructure.repository.jpa.JpaTaskExecutionRepository;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JPA 版任务执行仓储。
 * @author zhuang03@qq.com
 * @date 2026-05-30 05:22:56
 */
public class JpaTaskExecutionRepositoryImpl implements TaskExecutionRepository {

    private final JpaTaskExecutionRepository jpaRepository;

    public JpaTaskExecutionRepositoryImpl(JpaTaskExecutionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(TaskExecution execution) {
        jpaRepository.save(toModel(execution));
    }

    @Override
    public Optional<TaskExecution> findByTaskId(String taskId) {
        return jpaRepository.findById(taskId).map(this::toDomain);
    }

    @Override
    public Optional<TaskExecution> findLatestBySessionId(String sessionId) {
        return jpaRepository.findTopBySessionIdOrderBySubmittedAtDesc(sessionId).map(this::toDomain);
    }

    @Override
    public List<TaskExecution> findBySessionId(String sessionId) {
        return jpaRepository.findBySessionIdOrderBySubmittedAtDesc(sessionId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<TaskExecution> findLoopDetected(int limit) {
        int pageSize = Math.max(1, limit);
        return jpaRepository.findByLoopDetectedTrueOrderByUpdatedAtDesc(PageRequest.of(0, pageSize))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countSubmittedSince(String sessionId, LocalDateTime submittedAfterInclusive) {
        return jpaRepository.countBySessionIdAndSubmittedAtGreaterThanEqual(sessionId, submittedAfterInclusive);
    }

    @Override
    public long countActiveBySessionId(String sessionId) {
        return jpaRepository.countBySessionIdAndStatusIn(sessionId, List.of(
                TaskExecutionStatus.SUBMITTED.name(),
                TaskExecutionStatus.DISPATCHED.name(),
                TaskExecutionStatus.RUNNING.name(),
                TaskExecutionStatus.CANCEL_REQUESTED.name()
        ));
    }

    private TaskExecutionModel toModel(TaskExecution domain) {
        TaskExecutionModel model = new TaskExecutionModel();
        model.setTaskId(domain.getTaskId());
        model.setSessionId(domain.getSessionId());
        model.setOutputStreamKey(domain.getOutputStreamKey());
        model.setDispatchMessageId(domain.getDispatchMessageId());
        model.setConsumerGroup(domain.getConsumerGroup());
        model.setConsumerName(domain.getConsumerName());
        model.setWorkerId(domain.getWorkerId());
        model.setStatus(domain.getStatus() != null ? domain.getStatus().name() : TaskExecutionStatus.SUBMITTED.name());
        model.setCancelRequested(domain.isCancelRequested());
        model.setCancelReason(domain.getCancelReason());
        model.setLeaseExpiresAt(domain.getLeaseExpiresAt());
        model.setSubmittedAt(domain.getSubmittedAt());
        model.setStartedAt(domain.getStartedAt());
        model.setFinishedAt(domain.getFinishedAt());
        model.setUpdatedAt(domain.getUpdatedAt());
        model.setLastEventId(domain.getLastEventId());
        model.setTerminalEvent(domain.getTerminalEvent());
        model.setErrorMessage(domain.getErrorMessage());
        model.setEventCount(domain.getEventCount());
        model.setToolCallCount(domain.getToolCallCount());
        model.setStepStartCount(domain.getStepStartCount());
        model.setRepeatedToolCallCount(domain.getRepeatedToolCallCount());
        model.setLoopDetected(domain.isLoopDetected());
        model.setLastToolFingerprint(domain.getLastToolFingerprint());
        model.setWarnings(domain.getWarnings() != null ? new ArrayList<>(domain.getWarnings()) : new ArrayList<>());
        model.setReplayEvents(domain.getReplayEvents() != null ? new ArrayList<>(domain.getReplayEvents()) : new ArrayList<>());
        return model;
    }

    private TaskExecution toDomain(TaskExecutionModel model) {
        TaskExecution domain = new TaskExecution();
        domain.setTaskId(model.getTaskId());
        domain.setSessionId(model.getSessionId());
        domain.setOutputStreamKey(model.getOutputStreamKey());
        domain.setDispatchMessageId(model.getDispatchMessageId());
        domain.setConsumerGroup(model.getConsumerGroup());
        domain.setConsumerName(model.getConsumerName());
        domain.setWorkerId(model.getWorkerId());
        domain.setStatus(model.getStatus() != null ? TaskExecutionStatus.valueOf(model.getStatus()) : TaskExecutionStatus.SUBMITTED);
        domain.setCancelRequested(model.isCancelRequested());
        domain.setCancelReason(model.getCancelReason());
        domain.setLeaseExpiresAt(model.getLeaseExpiresAt());
        domain.setSubmittedAt(model.getSubmittedAt());
        domain.setStartedAt(model.getStartedAt());
        domain.setFinishedAt(model.getFinishedAt());
        domain.setUpdatedAt(model.getUpdatedAt());
        domain.setLastEventId(model.getLastEventId());
        domain.setTerminalEvent(model.getTerminalEvent());
        domain.setErrorMessage(model.getErrorMessage());
        domain.setEventCount(model.getEventCount());
        domain.setToolCallCount(model.getToolCallCount());
        domain.setStepStartCount(model.getStepStartCount());
        domain.setRepeatedToolCallCount(model.getRepeatedToolCallCount());
        domain.setLoopDetected(model.isLoopDetected());
        domain.setLastToolFingerprint(model.getLastToolFingerprint());
        domain.setWarningsSafe(model.getWarnings());
        domain.setReplayEventsSafe(model.getReplayEvents());
        return domain;
    }
}

