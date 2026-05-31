package com.imooc.manus.springai.session;

/**
 * 会话状态加载/更新接口。
 *
 * <p>
 * manus-spring-ai 模块本身不依赖 JPA/数据库，通过此接口解耦，
 * 由 manus-api 提供基于 JPA 的实现。
 * </p>
 *
 * <p><b>扩展点：</b>可替换为基于 Redis 等存储的实现。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-29 05:07:15
 */
public interface SessionStateLoader {

    /**
     * 获取指定会话的当前状态字符串。
     * 返回值为 "PENDING"、"RUNNING"、"WAITING"、"COMPLETED" 等。
     *
     * @param sessionId 会话id
     * @return 会话状态字符串，不存在时返回 "PENDING"
     */
    String getSessionStatus(String sessionId);

    /**
     * 获取指定会话最新规划（Plan）的 JSON 序列化字符串。
     *
     * @param sessionId 会话id
     * @return Plan JSON 字符串，不存在时返回 null
     */
    String getLatestPlanJson(String sessionId);

    /**
     * 更新指定会话的状态。
     *
     * @param sessionId 会话id
     * @param status    新状态字符串（"RUNNING"/"WAITING"/"COMPLETED" 等）
     */
    void updateSessionStatus(String sessionId, String status);

    /**
     * 保存 Plan JSON 到会话（用于持久化规划结果）。
     *
     * @param sessionId 会话id
     * @param planJson  Plan 的 JSON 字符串
     */
    void savePlanJson(String sessionId, String planJson);
}

