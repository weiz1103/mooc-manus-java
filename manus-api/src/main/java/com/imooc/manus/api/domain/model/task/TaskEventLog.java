package com.imooc.manus.api.domain.model.task;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 任务事件日志条目。
 *
 * <p>作为 task replay / offline evaluation 的主存储，
 * 与 {@link TaskExecution} 的聚合统计信息分离。</p>
 */
@Data
public class TaskEventLog {

    private String id;
    private String sessionId;
    private String taskId;
    private String eventId;
    private String eventType;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;

    public static TaskEventLog of(String sessionId,
                                  String taskId,
                                  String eventId,
                                  String eventType,
                                  Map<String, Object> payload) {
        TaskEventLog log = new TaskEventLog();
        log.sessionId = sessionId;
        log.taskId = taskId;
        log.eventId = eventId;
        log.eventType = eventType;
        log.payload = payload != null ? new LinkedHashMap<>(payload) : new LinkedHashMap<>();
        log.createdAt = LocalDateTime.now();
        return log;
    }
}

