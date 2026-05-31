package io.github.weiz1103.agentflow.api.domain.model.message;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户传递的消息Domain模型。
 * <p>
 * 包含用户发送的文本消息以及上传的附件ID列表。
 * </p>
 *
 * @param message     用户发送的消息内容
 * @param attachments 用户发送的附件（文件ID列表。
 * @author zhuang03@qq.com
 * @date 2026-05-27 20:26:12
 */
public record Message(
        String message,
        List<String> attachments
) {
    /**
     * 创建仅含文本的消。
     *
     * @param message 消息文本
     * @return Message实例
     */
    public static Message of(String message) {
        return new Message(message, new ArrayList<>());
    }

    /**
     * 创建含附件的消息
     *
     * @param message     消息文本
     * @param attachments 附件ID列表
     * @return Message实例
     */
    public static Message of(String message, List<String> attachments) {
        return new Message(message, attachments != null ? attachments : new ArrayList<>());
    }
}


