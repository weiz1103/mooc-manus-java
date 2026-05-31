package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * role=user 表示用户消息，role=assistant 表示 AI 回复。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 02:40:03
 */
public class MessageEvent extends BaseEvent {

    private String role = "assistant";  // "user" | "assistant"
    private String message = "";
    private List<String> attachments = new ArrayList<>();

    @Override
    public String getType() {
        return "message";
    }
}
