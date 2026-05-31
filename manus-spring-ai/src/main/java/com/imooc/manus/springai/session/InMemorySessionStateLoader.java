package com.imooc.manus.springai.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionStateLoader 的内存实现（用于开发、测试或单进程部署）。
 * <p>
 * 生产环境请替换为基于 JPA / Redis 的持久化实现（由 manus-api 层注入）。
 * </p>
 *
 * <p><b>扩展点：</b> 继承此类可添加 TTL 过期、分布式锁等能力。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-26 11:18:36
 */
public class InMemorySessionStateLoader implements SessionStateLoader {

    private final Map<String, String> statusStore   = new ConcurrentHashMap<>();
    private final Map<String, String> planJsonStore = new ConcurrentHashMap<>();

    @Override
    public String getSessionStatus(String sessionId) {
        return statusStore.getOrDefault(sessionId, "PENDING");
    }

    @Override
    public String getLatestPlanJson(String sessionId) {
        return planJsonStore.get(sessionId);
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        statusStore.put(sessionId, status);
    }

    @Override
    public void savePlanJson(String sessionId, String planJson) {
        planJsonStore.put(sessionId, planJson);
    }

    /** 测试辅助：清除所有状态 */
    public void clearAll() {
        statusStore.clear();
        planJsonStore.clear();
    }
}

