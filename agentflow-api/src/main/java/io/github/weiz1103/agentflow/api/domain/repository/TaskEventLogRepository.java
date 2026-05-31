package io.github.weiz1103.agentflow.api.domain.repository;

import io.github.weiz1103.agentflow.api.domain.model.task.TaskEventLog;

import java.util.List;

/**
 * 任务事件日志仓储。
 * @author zhuang03@qq.com
 * @date 2026-05-30 03:10:26
 */
public interface TaskEventLogRepository {

    void appendIfAbsent(TaskEventLog eventLog);

    List<TaskEventLog> findByTaskId(String taskId);
}


