package io.github.weiz1103.agentflow.api.infrastructure.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会话JPA模型，对应数据库中的sessions表。
 *

 */
@Entity
@Table(name = "sessions")
public class SessionModel {

    @Id
    @Column(name = "id", length = 255)
    private String id;

    @Column(name = "sandbox_id", length = 255)
    private String sandboxId;

    @Column(name = "task_id", length = 255)
    private String taskId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "unread_message_count")
    private int unreadMessageCount = 0;

    @Column(name = "latest_message", columnDefinition = "TEXT")
    private String latestMessage;

    @Column(name = "latest_message_at")
    private LocalDateTime latestMessageAt;

    /** JSONB格式存储事件列表 */
    @Column(name = "events", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<Map<String, Object>> events;

    /** JSONB格式存储文件列表 */
    @Column(name = "files", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private List<Map<String, Object>> files;

    /** JSONB格式存储记忆（键为agentName，值为Memory序列化） */
    @Column(name = "memories", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private Map<String, Object> memories;

    @Column(name = "status", length = 50)
    private String status = "PENDING";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ======================== Getters & Setters ========================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSandboxId() { return sandboxId; }
    public void setSandboxId(String sandboxId) { this.sandboxId = sandboxId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getUnreadMessageCount() { return unreadMessageCount; }
    public void setUnreadMessageCount(int unreadMessageCount) { this.unreadMessageCount = unreadMessageCount; }

    public String getLatestMessage() { return latestMessage; }
    public void setLatestMessage(String latestMessage) { this.latestMessage = latestMessage; }

    public LocalDateTime getLatestMessageAt() { return latestMessageAt; }
    public void setLatestMessageAt(LocalDateTime latestMessageAt) { this.latestMessageAt = latestMessageAt; }

    public List<Map<String, Object>> getEvents() { return events; }
    public void setEvents(List<Map<String, Object>> events) { this.events = events; }

    public List<Map<String, Object>> getFiles() { return files; }
    public void setFiles(List<Map<String, Object>> files) { this.files = files; }

    public Map<String, Object> getMemories() { return memories; }
    public void setMemories(Map<String, Object> memories) { this.memories = memories; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}


