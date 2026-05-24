package com.imooc.manus.springai.tool.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.springai.domain.port.A2APort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.List;

/**
 * A2A 工具回调提供者。
 * <p>
 * 将 get_remote_agent_cards 和 call_remote_agent 两个工具封装为
 * Spring AI {@link ToolCallback}，供 Agent 使用。
 * </p>
 * <p>
 * <b>扩展点：</b> 可在 {@link A2APort} 实现层添加 Agent 服务发现、熔断降级等能力。
 * </p>
 */
public class A2AToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(A2AToolCallbackProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final A2APort a2aPort;

    public A2AToolCallbackProvider(A2APort a2aPort) {
        this.a2aPort = a2aPort;
    }

    /**
     * 构建 A2A 工具回调列表（固定两个工具）。
     *
     * @return ToolCallback 列表
     */
    public List<ToolCallback> buildToolCallbacks() {
        ToolCallback getCards = FunctionToolCallback.builder("get_remote_agent_cards",
                        (EmptyInput ignored) -> {
                            try {
                                return OBJECT_MAPPER.writeValueAsString(a2aPort.getRemoteAgentCards());
                            } catch (Exception e) {
                                logger.error("get_remote_agent_cards 执行失败: {}", e.getMessage());
                                return "{\"success\":false}";
                            }
                        })
                .description("获取可远程调用的 Agent 卡片信息，包含 Agent id、名称、描述、技能、请求端点等。")
                .inputType(EmptyInput.class)
                .build();

        ToolCallback callAgent = FunctionToolCallback.builder("call_remote_agent",
                        (CallRemoteAgentInput input) -> {
                            try {
                                return OBJECT_MAPPER.writeValueAsString(
                                        a2aPort.callRemoteAgent(input.id(), input.query()));
                            } catch (Exception e) {
                                logger.error("call_remote_agent 执行失败: {}", e.getMessage());
                                return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
                            }
                        })
                .description("根据传递的 id + query（分配给远程 Agent 完成的任务 query）调用远程 Agent。")
                .inputType(CallRemoteAgentInput.class)
                .build();

        return List.of(getCards, callAgent);
    }

    /** 空参数输入（无参工具占位） */
    public record EmptyInput() {}

    /** 远程 Agent 调用参数 */
    public record CallRemoteAgentInput(String id, String query) {}
}

