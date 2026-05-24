package com.imooc.manus.api.domain.model.agent;

import java.util.Map;

/**
 * 智能体执行请求对象 (强类型替代原 Python 中的 kwargs)
 *
 * @param input   核心输入内容（用户输入或具体的 Step 描述）
 * @param payload 附加动态参数（可选）
 */
public record AgentRequest(
        String input,
        Map<String, Object> payload
) {
    public static AgentRequest of(String input) {
        return new AgentRequest(input, Map.of());
    }
}