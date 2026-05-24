package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.imooc.manus.api.domain.model.toolresult.ToolResult;

import java.util.Map;

/**
 * 工具事件。
 * 对应Python中的 ToolEvent。
 *
 * @author thezehui@gmail.com
 */
public class ToolEvent extends BaseEvent {

    /** 工具调用id */
    @JsonProperty("tool_call_id")
    @JsonAlias("toolCallId")
    private String toolCallId;

    /** 工具箱/工具集的名字 */
    @JsonProperty("tool_name")
    @JsonAlias("toolName")
    private String toolName;

    /** 工具扩展内容（浏览器截图、搜索结果等，序列化为Object） */
    @JsonProperty("tool_content")
    @JsonAlias("toolContent")
    private Object toolContent;

    /** LLM调用函数/工具名字 */
    @JsonProperty("function_name")
    @JsonAlias("functionName")
    private String functionName;

    /** LLM生成的工具调用参数 */
    @JsonProperty("function_args")
    @JsonAlias("functionArgs")
    private Map<String, Object> functionArgs;

    /** 工具调用结果 */
    @JsonProperty("function_result")
    @JsonAlias("functionResult")
    private ToolResult<?> functionResult;

    /** 工具事件状态 */
    @JsonProperty("status")
    private ToolEventStatus status;

    public ToolEvent() {
        super("tool");
        this.status = ToolEventStatus.CALLING;
    }

    public ToolEvent(
            String toolCallId,
            String toolName,
            String functionName,
            Map<String, Object> functionArgs,
            ToolEventStatus status
    ) {
        super("tool");
        this.toolCallId = toolCallId;
        this.toolName = toolName;
        this.functionName = functionName;
        this.functionArgs = functionArgs;
        this.status = status;
    }

    public String getToolCallId() { return toolCallId; }
    public void setToolCallId(String toolCallId) { this.toolCallId = toolCallId; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public Object getToolContent() { return toolContent; }
    public void setToolContent(Object toolContent) { this.toolContent = toolContent; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public Map<String, Object> getFunctionArgs() { return functionArgs; }
    public void setFunctionArgs(Map<String, Object> functionArgs) { this.functionArgs = functionArgs; }

    public ToolResult<?> getFunctionResult() { return functionResult; }
    public void setFunctionResult(ToolResult<?> functionResult) { this.functionResult = functionResult; }

    public ToolEventStatus getStatus() { return status; }
    public void setStatus(ToolEventStatus status) { this.status = status; }
}

