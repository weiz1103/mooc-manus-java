package com.imooc.manus.springai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

/**
 * 消息工具服务，提供向用户发送通知消息和询问消息的能力。
 * <p>
 * 对应 Python 版 MessageTool。
 * </p>
 * <p>
 * <b>扩展点：</b> 覆盖 {@link #messageNotifyUser} 和 {@link #messageAskUser} 可接入
 * 推送通知、邮件等渠道。
 * </p>
 */
public class MessageToolService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public MessageToolService() {
    }

    @Tool(name = "message_notify_user",
          description = "向用户发送消息，且无需用户回答。用于确认收到消息、提供进度更新、" +
                        "报告任务完成情况，或解释处理方式的变更。")
    public String messageNotifyUser(
            @ToolParam(description = "要发送给用户的通知文本") String text,
            @ToolParam(description = "附件文件路径列表（可选）", required = false) List<String> attachments
    ) {
        return serialize(Map.of("success", true, "data", "Message sent to user"));
    }

    @Tool(name = "message_ask_user",
          description = "向用户提问并等待回答。用于：请求澄清、要求确认、或收集额外信息。")
    public String messageAskUser(
            @ToolParam(description = "要向用户提出的问题") String text,
            @ToolParam(description = "附件列表（可选）", required = false) List<String> attachments,
            @ToolParam(description = "是否建议用户接管操作（可选，值为 browser）", required = false) String suggestUserTakeover
    ) {
        return serialize(Map.of("success", true, "data", "Waiting for user response"));
    }

    // ---- 工具名称常量，供 Agent 逻辑判断 ----
    public static final String TOOL_MESSAGE_NOTIFY = "message_notify_user";
    public static final String TOOL_MESSAGE_ASK    = "message_ask_user";

    private String serialize(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"序列化失败\"}";
        }
    }
}
