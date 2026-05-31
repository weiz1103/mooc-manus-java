package com.imooc.manus.api.domain.model.agent;

import java.util.Map;

/**
 *
 * @param input   核心输入内容（用户输入或具体的 Step 描述）
 * @param payload 附加动态参数（可选）
 * @author zhuang03@qq.com
 * @date 2026-05-29 09:53:48
 */
public record AgentRequest(
        String input,
        Map<String, Object> payload
) {
    public static AgentRequest of(String input) {
        return new AgentRequest(input, Map.of());
    }
}