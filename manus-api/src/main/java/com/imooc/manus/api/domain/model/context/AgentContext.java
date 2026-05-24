package com.imooc.manus.api.domain.model.context;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能体运行时上下文 (Agent Context)
 * 迁移自 Python 项目中的 Context 字典机制。
 * 核心逻辑：利用 Record 的不可变性保证基础属性安全，利用 ConcurrentHashMap 保证共享状态的并发安全。
 *
 * @param sessionId    会话唯一标识（对应数据库中的 Session ID）
 * @param traceId      全链路追踪 ID（用于日志排查）
 * @param chatMemory   Spring AI 标准会话记忆体（用于存储历史对话）
 * @param sharedMemory Agent 之间共享的动态变量字典（如中间执行结果）
 */
public record AgentContext(
        String sessionId,
        String traceId,
        ChatMemory chatMemory,
        Map<String, Object> sharedMemory
) {
    /**
     * 紧凑型构造器 (Compact Constructor)
     * 确保传入的 Map 在高并发流式输出场景下绝对线程安全
     */
    public AgentContext {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID 不能为空");
        }
        traceId = (traceId == null) ? UUID.randomUUID().toString() : traceId;
        chatMemory = (chatMemory == null)
                ? MessageWindowChatMemory.builder()
                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                        .build()
                : chatMemory;
        sharedMemory = new ConcurrentHashMap<>(sharedMemory != null ? sharedMemory : Map.of());
    }

    /**
     * 静态工厂方法：为新会话快速构建初始上下文
     *
     * @param sessionId 会话 ID
     * @return 初始化的 AgentContext
     */
    public static AgentContext create(String sessionId) {
        return new AgentContext(sessionId, null, null, null);
    }
}