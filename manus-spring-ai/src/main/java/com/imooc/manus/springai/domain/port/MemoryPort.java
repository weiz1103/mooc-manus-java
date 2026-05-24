package com.imooc.manus.springai.domain.port;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 会话记忆存储端口。
 * <p>
 * 对接 Spring AI 的 {@link org.springframework.ai.chat.memory.ChatMemory} 语义，
 * 但使用 manus 自身的 sessionId + agentName 双维度定位记忆，
 * 底层持久化由 manus-api 层注入实现。
 * </p>
 */
public interface MemoryPort {

    /**
     * 获取指定会话 + Agent 的历史消息列表。
     *
     * @param sessionId 会话 id
     * @param agentName Agent 名称（planner / react）
     * @return Spring AI Message 列表
     */
    List<Message> getMessages(String sessionId, String agentName);

    /**
     * 向指定会话 + Agent 追加消息。
     *
     * @param sessionId 会话 id
     * @param agentName Agent 名称
     * @param messages  要追加的消息列表
     */
    void addMessages(String sessionId, String agentName, List<Message> messages);

    /**
     * 压缩/清理历史消息（保留系统消息，压缩中间消息）。
     *
     * @param sessionId 会话 id
     * @param agentName Agent 名称
     */
    void compact(String sessionId, String agentName);

    /**
     * 回滚最后一条消息（用于中断恢复）。
     *
     * @param sessionId 会话 id
     * @param agentName Agent 名称
     */
    void rollBack(String sessionId, String agentName);

    /**
     * 清空指定 Agent 记忆。
     *
     * @param sessionId 会话 id
     * @param agentName Agent 名称
     */
    void clear(String sessionId, String agentName);
}

