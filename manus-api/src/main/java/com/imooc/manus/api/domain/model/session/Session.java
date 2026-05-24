package com.imooc.manus.api.domain.model.session;

import com.imooc.manus.api.domain.model.event.BaseEvent;
import com.imooc.manus.api.domain.model.event.PlanEvent;
import com.imooc.manus.api.domain.model.file.FileMeta;
import com.imooc.manus.api.domain.model.memory.Memory;
import com.imooc.manus.api.domain.model.plan.Plan;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 会话领域模型。
 * <p>
 * 对应Python中的 Session Pydantic BaseModel。
 * 使用可变POJO，因为会话状态在执行过程中会被修改。
 * </p>
 *
 * @author thezehui@gmail.com
 */
public class Session {

    /** 会话id */
    private String id;

    /** 沙箱id */
    private String sandboxId;

    /** 任务id */
    private String taskId;

    /** 标题 */
    private String title;

    /** 未读消息数 */
    private int unreadMessageCount;

    /** 最新消息 */
    private String latestMessage;

    /**
     * 业务方法：开始执行任务
     */
    public void start() {
        // 允许从任何状态（包含 COMPLETED 和 WAITING）重新进入 RUNNING 状态
        // 这样可以支持同一会话内的多轮追问
        this.status = SessionStatus.RUNNING;
    }

    /**
     * 业务方法：等待用户输入
     */
    public void waitForInput() {
        if (this.status == SessionStatus.RUNNING) {
            this.status = SessionStatus.WAITING;
        }
    }

    /**
     * 业务方法：完成会话
     */
    public void complete() {
        this.status = SessionStatus.COMPLETED;
    }

    /**
     * 基础设施专用：从持久层还原状态（禁止在业务逻辑中调用）。
     * 仅供 JPA Repository 反序列化时使用。
     */
    public void restore(SessionStatus status) {
        this.status = status;
    }

    /** 最新消息时间 */
    private LocalDateTime latestMessageAt;

    /** 事件列表 */
    private List<BaseEvent> events;

    /** 文件列表 */
    private List<FileMeta> files;

    /** 记忆（按Agent名字存储） */
    private Map<String, Memory> memories;

    /** 状态 */
    private SessionStatus status;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public Session() {
        this.id = UUID.randomUUID().toString();
        this.title = "";
        this.unreadMessageCount = 0;
        this.latestMessage = "";
        this.events = new ArrayList<>();
        this.files = new ArrayList<>();
        this.memories = new HashMap<>();
        this.status = SessionStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 获取会话中的最新规划。
     * 倒序遍历会话中所有事件消息，找到第一个PlanEvent并提取规划后返回。
     * 对应Python的 Session.get_latest_plan()
     *
     * @return 最新规划（Optional）
     */
    public Optional<Plan> getLatestPlan() {
        for (int i = events.size() - 1; i >= 0; i--) {
            if (events.get(i) instanceof PlanEvent planEvent) {
                return Optional.ofNullable(planEvent.getPlan());
            }
        }
        return Optional.empty();
    }

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

    public List<BaseEvent> getEvents() { return events; }
    public void setEvents(List<BaseEvent> events) { this.events = events; }

    public List<FileMeta> getFiles() { return files; }
    public void setFiles(List<FileMeta> files) { this.files = files; }

    public Map<String, Memory> getMemories() { return memories; }
    public void setMemories(Map<String, Memory> memories) { this.memories = memories; }

    public SessionStatus getStatus() { return status; }

    /** 只允许包内（状态机方法）修改状态，外部禁止直接赋值 */
    void setStatus(SessionStatus status) { this.status = status; }

    public boolean isRunning() { return status == SessionStatus.RUNNING; }
    public boolean isWaiting() { return status == SessionStatus.WAITING; }
    public boolean isCompleted() { return status == SessionStatus.COMPLETED; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

