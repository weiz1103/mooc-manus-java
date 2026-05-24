package com.imooc.manus.springai.memory;

import java.util.List;
import java.util.Map;

/**
 * Agent记忆持久化接口。
 * 对应Python中的 _uow.session.get_memory / save_memory 操作。
 *
 * <p>
 * 设计原则：
 * - 消息存储为 List&lt;Map&lt;String, Object&gt;&gt;（OpenAI兼容原始格式）
 * - 键为 sessionId + agentName 的组合，确保每个会话每个Agent独立存储
 * - 默认实现为内存存储，生产环境通过 JpaAgentMemoryStore 持久化到数据库
 * </p>
 *
 * <p><b>扩展点：</b>可通过实现此接口替换为 Redis、DB 等持久化后端。</p>
 *
 * @author thezehui@gmail.com
 */
public interface AgentMemoryStore {

    /**
     * 加载指定会话+Agent的历史消息列表。
     * 消息格式为 OpenAI API 原始格式（role/content/tool_calls 等字段）。
     *
     * @param sessionId 会话id
     * @param agentName Agent名称（"planner" 或 "react"）
     * @return 历史消息列表，首次调用返回空列表
     */
    List<Map<String, Object>> load(String sessionId, String agentName);

    /**
     * 保存指定会话+Agent的消息列表（全量覆盖保存）。
     *
     * @param sessionId 会话id
     * @param agentName Agent名称
     * @param messages  要保存的消息列表
     */
    void save(String sessionId, String agentName, List<Map<String, Object>> messages);

    /**
     * 压缩指定Agent的记忆，移除重量级内容（浏览器页面内容等），以减少 Token 消耗。
     * 对应Python的 Memory.compact()。
     * 默认实现为空操作，子类可按需重写。
     *
     * @param sessionId 会话id
     * @param agentName Agent名称
     */
    default void compact(String sessionId, String agentName) {
        List<Map<String, Object>> messages = load(sessionId, agentName);
        for (Map<String, Object> message : messages) {
            // 1. 压缩 tool 消息中的浏览器相关内容
            if ("tool".equals(message.get("role"))) {
                String functionName = (String) message.get("function_name");
                if ("browser_view".equals(functionName) || "browser_navigate".equals(functionName)
                        || "browserView".equals(functionName) || "browserNavigate".equals(functionName)) {
                    message.put("content", "(removed)");
                }
            }
            // 2. 移除 reasoning_content（DeepSeek思考链内容），避免重复占用上下文
            message.remove("reasoning_content");
        }
        save(sessionId, agentName, messages);
    }

    /**
     * 清除指定会话+Agent的所有记忆。
     *
     * @param sessionId 会话id
     * @param agentName Agent名称
     */
    default void clear(String sessionId, String agentName) {
        save(sessionId, agentName, new java.util.ArrayList<>());
    }
}

