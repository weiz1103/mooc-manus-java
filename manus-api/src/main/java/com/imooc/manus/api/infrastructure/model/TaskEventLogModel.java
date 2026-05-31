package com.imooc.manus.api.infrastructure.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * task_event_log 表对应实体。
 */
@Entity
@Table(name = "task_event_log", indexes = {
        @Index(name = "idx_task_event_log_task_created", columnList = "task_id, created_at"),
        @Index(name = "idx_task_event_log_session_created", columnList = "session_id, created_at")
})
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-29 12:40:54
 */
public class TaskEventLogModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    @Column(name = "task_id", nullable = false, length = 255)
    private String taskId;

    @Column(name = "event_id", length = 255)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    @Type(JsonBinaryType.class)
    private Map<String, Object> payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

