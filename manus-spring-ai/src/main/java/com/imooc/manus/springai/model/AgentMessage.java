package com.imooc.manus.springai.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户消息模型，对应Python中的 Message Pydantic BaseModel。
 * 包含用户发送的文本消息和附件文件路径列表。
 *
 * @param message     用户发送的消息内容
 * @param attachments 用户发送的附件（文件绝对路径列表）
 *
 * @author thezehui@gmail.com
 */
public record AgentMessage(
        String message,
        List<String> attachments
) {
    /**
     * 创建仅含文本的消息
     *
     * @param message 消息文本
     * @return AgentMessage实例
     */
    public static AgentMessage of(String message) {
        return new AgentMessage(message, new ArrayList<>());
    }

    /**
     * 创建含附件的消息
     *
     * @param message     消息文本
     * @param attachments 附件路径列表
     * @return AgentMessage实例
     */
    public static AgentMessage of(String message, List<String> attachments) {
        return new AgentMessage(message, attachments != null ? attachments : new ArrayList<>());
    }
}

