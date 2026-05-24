package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 基础事件类型。
 * <p>
 * 对应Python中的 BaseEvent Pydantic BaseModel。
 * 所有事件类型均继承此类。
 * 使用Jackson多态配置支持事件类型判别。
 * </p>
 *
 * @author thezehui@gmail.com
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = BaseEvent.class, visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlanEvent.class, name = "plan"),
        @JsonSubTypes.Type(value = TitleEvent.class, name = "title"),
        @JsonSubTypes.Type(value = StepEvent.class, name = "step"),
        @JsonSubTypes.Type(value = MessageEvent.class, name = "message"),
        @JsonSubTypes.Type(value = ToolEvent.class, name = "tool"),
        @JsonSubTypes.Type(value = WaitEvent.class, name = "wait"),
        @JsonSubTypes.Type(value = ErrorEvent.class, name = "error"),
        @JsonSubTypes.Type(value = DoneEvent.class, name = "done"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent {

    /** 事件id */
    @JsonProperty("id")
    private String id;

    /** 事件的类型 */
    @JsonProperty("type")
    private String type;

    /** 事件创建时间 */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public BaseEvent() {
        this.id = UUID.randomUUID().toString();
        this.type = "";
        this.createdAt = LocalDateTime.now();
    }

    public BaseEvent(String type) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

