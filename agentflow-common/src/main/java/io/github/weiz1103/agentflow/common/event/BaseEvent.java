package io.github.weiz1103.agentflow.common.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 所。Agent 事件的抽象基类。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MessageEvent.class, name = "message"),
    @JsonSubTypes.Type(value = PlanEvent.class,    name = "plan"),
    @JsonSubTypes.Type(value = TitleEvent.class,   name = "title"),
    @JsonSubTypes.Type(value = StepEvent.class,    name = "step"),
    @JsonSubTypes.Type(value = ToolEvent.class,    name = "tool"),
    @JsonSubTypes.Type(value = WaitEvent.class,    name = "wait"),
    @JsonSubTypes.Type(value = ErrorEvent.class,   name = "error"),
    @JsonSubTypes.Type(value = DoneEvent.class,    name = "done")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEvent {

    private String id = UUID.randomUUID().toString();

    /** 事件创建时间 */
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 获取事件类型字符串（用于 SSE event 字段。*/
    public abstract String getType();
}

