package com.imooc.manus.api.domain.repository;

import com.imooc.manus.api.domain.model.task.TaskEventLog;

import java.util.List;

/**
 * 任务事件日志仓储。
 */
public interface TaskEventLogRepository {

    void appendIfAbsent(TaskEventLog eventLog);

    List<TaskEventLog> findByTaskId(String taskId);
}

