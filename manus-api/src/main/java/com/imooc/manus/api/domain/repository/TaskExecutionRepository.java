package com.imooc.manus.api.domain.repository;

import com.imooc.manus.api.domain.model.task.TaskExecution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务执行仓储。
 */
public interface TaskExecutionRepository {

    void save(TaskExecution execution);

    Optional<TaskExecution> findByTaskId(String taskId);

    Optional<TaskExecution> findLatestBySessionId(String sessionId);

    List<TaskExecution> findBySessionId(String sessionId);

    List<TaskExecution> findLoopDetected(int limit);

    long countSubmittedSince(String sessionId, LocalDateTime submittedAfterInclusive);

    long countActiveBySessionId(String sessionId);
}

