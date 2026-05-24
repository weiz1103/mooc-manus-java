package com.imooc.manus.api.domain.model.agent;

/**
 * 智能体执行响应对象
 * 使用泛型 T 承载不同 Agent 返回的专属数据结构（如 Planner 返回 Plan，ReAct 返回 String）
 *
 * @param success 是否执行成功
 * @param content AI 返回的原始文本/思考过程
 * @param data    结构化解析后的对象 (强类型)
 */
public record AgentResponse<T>(
        boolean success,
        String content,
        T data
) {
    public static <T> AgentResponse<T> success(String content, T data) {
        return new AgentResponse<>(true, content, data);
    }

    public static <T> AgentResponse<T> error(String content) {
        return new AgentResponse<>(false, content, null);
    }
}