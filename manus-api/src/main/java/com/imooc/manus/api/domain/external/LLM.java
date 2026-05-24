package com.imooc.manus.api.domain.external;

import java.util.List;
import java.util.Map;

/**
 * 用于Agent应用中与LLM进行交互的接口协议。
 * <p>
 * 对应Python中的 LLM Protocol。
 * 所有方法均为同步（运行在虚拟线程中，不影响响应性）。
 * messages使用 List&lt;Map&lt;String, Object&gt;&gt; 对应OpenAI API原始格式。
 * </p>
 *
 * @author thezehui@gmail.com
 */
public interface LLM {

    /**
     * 传递消息列表、工具列表、响应格式、工具选择策略调用LLM接口。
     *
     * @param messages       消息列表（OpenAI格式：role, content, tool_calls等）
     * @param tools          工具列表（OpenAI function schema格式），可为null
     * @param responseFormat 响应格式（如{"type": "json_object"}），可为null
     * @param toolChoice     工具选择策略（如"none"/"auto"），可为null
     * @return LLM返回的消息Map（role, content, tool_calls等）
     * @throws RuntimeException 调用失败时抛出异常
     */
    Map<String, Object> invoke(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            Map<String, Object> responseFormat,
            String toolChoice
    );

    /**
     * 只读属性，返回LLM的名字
     *
     * @return 模型名字
     */
    String getModelName();

    /**
     * 只读属性，返回LLM的温度
     *
     * @return 温度
     */
    double getTemperature();

    /**
     * 只读属性，返回LLM的最大生成token数
     *
     * @return 最大token数
     */
    int getMaxTokens();
}

