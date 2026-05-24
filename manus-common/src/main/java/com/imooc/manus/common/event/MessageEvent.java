package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息事件，对应 Python MessageEvent。
 * role=user 表示用户消息，role=assistant 表示 AI 回复。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MessageEvent extends BaseEvent {

    private String role = "assistant";  // "user" | "assistant"
    private String message = "";
    private List<String> attachments = new ArrayList<>();

    @Override
    public String getType() {
        return "message";
    }
}
