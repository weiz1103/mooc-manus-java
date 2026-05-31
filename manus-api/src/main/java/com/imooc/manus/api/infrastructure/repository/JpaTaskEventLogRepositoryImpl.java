package com.imooc.manus.api.infrastructure.repository;

import com.imooc.manus.api.domain.model.task.TaskEventLog;
import com.imooc.manus.api.domain.repository.TaskEventLogRepository;
import com.imooc.manus.api.infrastructure.model.TaskEventLogModel;
import com.imooc.manus.api.infrastructure.repository.jpa.JpaTaskEventLogRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA 版任务事件日志仓储。
 * @author zhuang03@qq.com
 * @date 2026-05-30 21:52:01
 */
public class JpaTaskEventLogRepositoryImpl implements TaskEventLogRepository {

    private final JpaTaskEventLogRepository jpaRepository;

    public JpaTaskEventLogRepositoryImpl(JpaTaskEventLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void appendIfAbsent(TaskEventLog eventLog) {
        if (eventLog == null || eventLog.getTaskId() == null || eventLog.getTaskId().isBlank()) {
            return;
        }
        if (eventLog.getEventId() != null && !eventLog.getEventId().isBlank()
                && jpaRepository.existsByTaskIdAndEventId(eventLog.getTaskId(), eventLog.getEventId())) {
            return;
        }
        jpaRepository.save(toModel(eventLog));
    }

    @Override
    public List<TaskEventLog> findByTaskId(String taskId) {
        return jpaRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private TaskEventLogModel toModel(TaskEventLog domain) {
        TaskEventLogModel model = new TaskEventLogModel();
        model.setId(domain.getId());
        model.setSessionId(domain.getSessionId());
        model.setTaskId(domain.getTaskId());
        model.setEventId(domain.getEventId());
        model.setEventType(domain.getEventType());
        model.setPayload(copyPayload(domain.getPayload()));
        model.setCreatedAt(domain.getCreatedAt());
        return model;
    }

    private TaskEventLog toDomain(TaskEventLogModel model) {
        TaskEventLog domain = new TaskEventLog();
        domain.setId(model.getId());
        domain.setSessionId(model.getSessionId());
        domain.setTaskId(model.getTaskId());
        domain.setEventId(model.getEventId());
        domain.setEventType(model.getEventType());
        domain.setPayload(copyPayload(model.getPayload()));
        domain.setCreatedAt(model.getCreatedAt());
        return domain;
    }

    private Map<String, Object> copyPayload(Map<String, Object> payload) {
        return payload == null ? new LinkedHashMap<>() : new LinkedHashMap<>(payload);
    }
}

