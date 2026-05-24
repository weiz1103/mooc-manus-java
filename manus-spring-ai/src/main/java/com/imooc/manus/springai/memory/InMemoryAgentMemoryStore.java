package com.imooc.manus.springai.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的 AgentMemoryStore 默认实现。
 * 对应Python中将记忆保存在内存中的行为（用于测试/开发）。
 *
 * <p>
 * 生产环境建议使用 JpaAgentMemoryStore（由 manus-api 模块提供）进行数据库持久化。
 * </p>
 *
 * @author thezehui@gmail.com
 */
public class InMemoryAgentMemoryStore implements AgentMemoryStore {

    /**
     * 内存存储：key = "sessionId:agentName"，value = 消息列表
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final ConcurrentHashMap<String, List<Map<String, Object>>> store = new ConcurrentHashMap<>();

    /**
     * 构造存储键
     *
     * @param sessionId 会话id
     * @param agentName Agent名称
     * @return 存储键
     */
    private String key(String sessionId, String agentName) {
        return sessionId + ":" + agentName;
    }

    @Override
    public List<Map<String, Object>> load(String sessionId, String agentName) {
        // 返回存储的消息副本，避免外部修改影响内部状态
        return new ArrayList<>(store.getOrDefault(key(sessionId, agentName), new ArrayList<>()));
    }

    @Override
    public void save(String sessionId, String agentName, List<Map<String, Object>> messages) {
        // 保存消息列表副本
        store.put(key(sessionId, agentName), new ArrayList<>(messages));
    }

    @Override
    public void clear(String sessionId, String agentName) {
        store.remove(key(sessionId, agentName));
    }
}

