package com.imooc.manus.springai.tool.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.springai.domain.port.McpPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具回调提供者。
 * <p>
 * 在运行时动态将 MCP Server 暴露的工具转换为 Spring AI {@link ToolCallback}，
 * 以便 {@link com.imooc.manus.springai.tool.registry.ToolCallbackRegistry} 统一注册。
 * </p>
 * <p>
 * <b>扩展点：</b> 重写 {@link #buildToolCallbacks()} 可在工具调用前后注入日志、鉴权等横切逻辑。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-28 19:09:43
 */
public class McpToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(McpToolCallbackProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final McpPort mcpPort;

    public McpToolCallbackProvider(McpPort mcpPort) {
        this.mcpPort = mcpPort;
    }

    /**
     * 将 MCP 工具定义动态构建为 ToolCallback 列表。
     *
     * @return ToolCallback 列表
     */
    @SuppressWarnings("unchecked")
    public List<ToolCallback> buildToolCallbacks() {
        List<ToolCallback> list = new ArrayList<>();
        List<Map<String, Object>> toolDefs = mcpPort.getTools();
        if (toolDefs == null) return list;

        for (Map<String, Object> toolDef : toolDefs) {
            Map<String, Object> function = (Map<String, Object>) toolDef.get("function");
            if (function == null) continue;

            String toolName    = (String) function.get("name");
            String description = (String) function.getOrDefault("description", toolName);

            list.add(FunctionToolCallback.builder(toolName,
                            (GenericArgsInput input) -> {
                                try {
                                    return OBJECT_MAPPER.writeValueAsString(
                                            mcpPort.invoke(toolName, input.args()));
                                } catch (Exception e) {
                                    logger.error("MCP 工具 [{}] 执行失败: {}", toolName, e.getMessage());
                                    return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
                                }
                            })
                    .description(description)
                    .inputType(GenericArgsInput.class)
                    .build());
        }
        return list;
    }

    /** 通用参数输入 record，供 FunctionCallback 使用 */
    public record GenericArgsInput(Map<String, Object> args) {}
}

