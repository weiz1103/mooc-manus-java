package io.github.weiz1103.agentflow.springai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.weiz1103.agentflow.common.event.*;
import io.github.weiz1103.agentflow.springai.config.SpringAIFlowConfig;
import io.github.weiz1103.agentflow.springai.memory.AgentMemoryStore;
import io.github.weiz1103.agentflow.springai.model.AgentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.tool.ToolCallback;

import java.util.*;
import java.util.function.Consumer;

/**
 * Spring AI 基础 Agent 抽象类。
 *
 * <p>
 * 设计要点。
 * - 使用 Spring AI ChatClient 替代自定。LLM 接口
 * - 通过 AgentMemoryStore 持久化对话历史（OpenAI 原始格式。
 * - 使用 internalToolExecutionEnabled(false) 禁用 ChatClient 自动工具执行，实现手动工具调用循。
 * - 工具调用循环中通过 Consumer&lt;BaseEvent&gt; 回调发送事件，替代Python。async generator
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-27 04:08:54
 */
public abstract class BaseSpringAIAgent {

    protected static final Logger logger = LoggerFactory.getLogger(BaseSpringAIAgent.class);
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    /** 当前会话id */
    protected final String sessionId;
    /** 记忆存储 */
    protected final AgentMemoryStore memoryStore;
    /** Spring AI ChatClient */
    protected final ChatClient chatClient;
    /** 工具回调列表 */
    protected final List<ToolCallback> toolCallbacks;
    /** 流程配置 */
    protected final SpringAIFlowConfig config;

    /**
     * 构造函数，完成基础 Agent 初始化。
     *
     * @param sessionId     会话id
     * @param memoryStore   记忆持久化存。
     * @param chatClient    Spring AI ChatClient
     * @param toolCallbacks 工具回调列表（从外部传入，不由本类创建）
     * @param config        流程配置
     */
    protected BaseSpringAIAgent(
            String sessionId,
            AgentMemoryStore memoryStore,
            ChatClient chatClient,
            List<ToolCallback> toolCallbacks,
            SpringAIFlowConfig config
    ) {
        this.sessionId = sessionId;
        this.memoryStore = memoryStore;
        this.chatClient = chatClient;
        this.toolCallbacks = toolCallbacks;
        this.config = config;
    }

    /**
     * 获取 Agent 名称（用于记忆存储的 key）。
     *
     * @return Agent 名称
     */
    public abstract String getName();

    /**
     * 获取 Agent 的系统预。prompt。
     *
     * @return 系统 prompt 字符。
     */
    protected abstract String getSystemPrompt();

    /**
     * 调用 LLM 并手动处理工具调用循环，。emitter 发送事件。
     *
     * <p>
     * 完整流程。
     * 1. 。memoryStore 加载历史记忆（含 system prompt。
     * 2. 将新。user 消息添加到记忆并持久。
         * 3. 调用 ChatClient（internalToolExecutionEnabled=false，禁止自动执行工具）
     * 4. 获取 AssistantMessage，判断是否有工具调用
     * 5. 若有工具调用：发。ToolEvent(CALLING) 。执行工具 。发。ToolEvent(CALLED) 。添加工具响应到记。。循环
     * 6. message_ask_user 工具调用时：发。MessageEvent 。发。WaitEvent 。返回 null（中断）
     * 7. 达到最大迭代次数时：发。ErrorEvent
     * 8. 无工具调用时：发。MessageEvent（最终内容）并返。
     * </p>
     *
     * @param query           本轮用户/内部查询文本
     * @param responseFormat  响应格式。json_object" 。null。
     * @param toolChoice      工具选择策略。none" 。null 表示 auto。
     * @return 最。AssistantMessage（内容消息），null 表示等待用户输入或出。
     */
    protected AssistantMessage invokeLlm(
            String query,
            String responseFormat,
            String toolChoice,
            Consumer<BaseEvent> emitter
    ) {
        // 1. 加载历史记忆
        List<Map<String, Object>> rawMessages = memoryStore.load(sessionId, getName());
        List<Message> currentMessages = toSpringAiMessages(rawMessages);

        // 2. 如果记忆为空，先添加 system prompt
        if (rawMessages.isEmpty()) {
            rawMessages.add(Map.of("role", "system", "content", getSystemPrompt()));
            currentMessages.add(new SystemMessage(getSystemPrompt()));
        }

        // 3. 添加本轮 user 消息
        rawMessages.add(Map.of("role", "user", "content", query));
        currentMessages.add(new UserMessage(query));
        memoryStore.save(sessionId, getName(), rawMessages);

        // 4. 构建 ChatClient 选项
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(config.modelName())
                .internalToolExecutionEnabled(false); // 禁用自动工具执行，手动控制工具调用循。

        if ("json_object".equals(responseFormat)) {
            // 启用 JSON Object 输出格式（PlannerAgent/ReActAgent 输出结构化数据）
            optionsBuilder.responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_OBJECT, null));
        }

        if ("none".equals(toolChoice)) {
            // PlannerAgent: 不允许调用工具，只输。JSON 内容
            optionsBuilder.toolChoice("none");
        }

        int iterations = 0;
        while (iterations < config.maxIterations()) {
            // 5.1 调用 ChatClient，获。AssistantMessage（proxyToolCalls=true，不自动执行工具。
            ChatResponse response = callWithRetry(currentMessages, optionsBuilder.build(), emitter);
            if (response == null) {
                return null;
            }

            AssistantMessage assistantMsg = response.getResult().getOutput();

            // 5.2 。AssistantMessage 存入记忆
            Map<String, Object> assistantRaw = toRawAssistantMessage(assistantMsg);
            rawMessages.add(assistantRaw);
            currentMessages.add(assistantMsg);
            memoryStore.save(sessionId, getName(), rawMessages);

            // 5.3 检查是否有工具调用
            List<AssistantMessage.ToolCall> toolCalls = assistantMsg.getToolCalls();
            if (toolCalls == null || toolCalls.isEmpty()) {
                // 无工具调。。LLM 已生成最终文本回答，退出循。
                return assistantMsg;
            }

            // Python 中每次只处理一个工具调。
            AssistantMessage.ToolCall toolCall = toolCalls.get(0);
            String toolCallId = toolCall.id() != null ? toolCall.id() : UUID.randomUUID().toString();
            String functionName = toolCall.name();
            Map<String, Object> functionArgs = parseArgs(toolCall.arguments());

            // 5.5 特殊处理 message_ask_user 。message_notify_user（直接发。MessageEvent，跳。ToolEvent 以防重复。
            if ("message_ask_user".equals(functionName) || "message_notify_user".equals(functionName)) {
                String askText = (String) functionArgs.getOrDefault("text", "");
                emitter.accept(MessageEvent.builder()
                        .role("assistant")
                        .message(askText)
                        .attachments(parseAttachments(functionArgs.get("attachments")))
                        .build());
            } else {
                emitter.accept(ToolEvent.builder()
                        .toolCallId(toolCallId)
                        .toolName(extractToolSetName(functionName))
                        .functionName(functionName)
                        .functionArgs(functionArgs)
                        .status(ToolEvent.ToolEventStatus.CALLING)
                        .build());
            }

            // 5.7 执行工具
            String toolResultJson = executeToolCallback(functionName, toolCall.arguments(), emitter);

            // 5.8 发送工具调用完成事件（仅针对非消息工具。
            if (!"message_ask_user".equals(functionName) && !"message_notify_user".equals(functionName)) {
                emitter.accept(ToolEvent.builder()
                        .toolCallId(toolCallId)
                        .toolName(extractToolSetName(functionName))
                        .functionName(functionName)
                        .functionArgs(functionArgs)
                        .toolContent(parseResult(toolResultJson))
                        .status(ToolEvent.ToolEventStatus.CALLED)
                        .build());
            }

            // 5.9 特殊处理 message_ask_user（CALLED 时发。WaitEvent 并中断流程）
            if ("message_ask_user".equals(functionName)) {
                emitter.accept(WaitEvent.builder().build());
                return null; // 返回 null 信号：等待用户输。
            }

            // 5.10 将工具响应添加到记忆
            Map<String, Object> toolRaw = new LinkedHashMap<>();
            toolRaw.put("role", "tool");
            toolRaw.put("tool_call_id", toolCallId);
            toolRaw.put("function_name", functionName);
            toolRaw.put("content", toolResultJson);
            rawMessages.add(toolRaw);

            List<ToolResponseMessage.ToolResponse> toolResponses = List.of(
                    new ToolResponseMessage.ToolResponse(toolCallId, functionName, toolResultJson));
            currentMessages.add(ToolResponseMessage.builder()
                    .responses(toolResponses)
                    .metadata(Map.of())
                    .build());
            memoryStore.save(sessionId, getName(), rawMessages);

            iterations++;
        }

        // 6. 达到最大迭代次。
        logger.warn("Agent[{}] 会话[{}] 迭代超过最大次。 {}", getName(), sessionId, config.maxIterations());
        emitter.accept(ErrorEvent.builder()
                .error("Agent迭代超过最大迭代次。 " + config.maxIterations() + ", 任务处理失败")
                .build());
        return null;
    }

    /**
     * 带重试的 ChatClient 调用。
     *
     * @param messages 消息列表
     * @param options  ChatClient 选项
     * @param emitter  事件发射器（用于发送错误事件）
     * @return ChatResponse，失败时返回 null
     */
    private ChatResponse callWithRetry(List<Message> messages, OpenAiChatOptions options, Consumer<BaseEvent> emitter) {
        Exception lastError = null;
        for (int i = 0; i < config.maxRetries(); i++) {
            try {
                ChatClient.CallResponseSpec spec = chatClient.prompt()
                        .messages(messages)
                        .toolCallbacks(toolCallbacks.toArray(new ToolCallback[0]))
                        .options(options)
                        .call();
                ChatResponse response = spec.chatResponse();

                AssistantMessage output = response.getResult().getOutput();
                if (output.getText() == null
                        && (output.getToolCalls() == null || output.getToolCalls().isEmpty())) {
                    logger.warn("LLM 返回了空内容，执行重。({}/{})", i + 1, config.maxRetries());
                    Thread.sleep(config.retryIntervalMs());
                    continue;
                }
                return response;
            } catch (Exception e) {
                lastError = e;
                logger.error("调用 LLM 发生错误 ({}/{}): {}", i + 1, config.maxRetries(), e.getMessage());
                try {
                    Thread.sleep(config.retryIntervalMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        logger.error("调用 LLM 失败，已达到最大重试次。 {}", config.maxRetries());
        if (lastError != null) {
            emitter.accept(ErrorEvent.builder()
                    .error("调用语言模型失败: " + lastError.getMessage())
                    .build());
        }
        return null;
    }

    /**
     * 执行指定工具回调。
     *
     * @param functionName 函数名称
     * @param argsJson     JSON 格式的参数字符串
     * @param emitter      事件发射。
     * @return 工具执行结果 JSON 字符。
     */
    protected String executeToolCallback(String functionName, String argsJson, Consumer<BaseEvent> emitter) {
        // 1. 查找匹配的工具回。
        for (ToolCallback callback : toolCallbacks) {
            if (functionName.equals(callback.getToolDefinition().name())) {
                // 2. 带重试执行工。
                Exception lastError = null;
                for (int i = 0; i < config.maxRetries(); i++) {
                    try {
                        return callback.call(argsJson);
                    } catch (Exception e) {
                        lastError = e;
                        logger.error("调用工具[{}]出错 ({}/{}): {}", functionName, i + 1, config.maxRetries(), e.getMessage());
                        try {
                            Thread.sleep(config.retryIntervalMs());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                // 3. 所有重试失败，返回错误结果
                String errMsg = lastError != null ? lastError.getMessage() : "工具执行失败";
                try {
                    return OBJECT_MAPPER.writeValueAsString(Map.of("success", false, "message", errMsg));
                } catch (Exception e) {
                    return "{\"success\": false, \"message\": \"" + errMsg + "\"}";
                }
            }
        }
        return "{\"success\": false, \"message\": \"未知工具: " + functionName + "\"}";
    }

    /**
     * 回滚 Agent 记忆，确保消息列表格式正确，避免 AI（工具调用消息）后直接接上人类消息。
     *
     * @param message 最新用户消息（用于处理 message_ask_user 的工具响应）
     */
    public void rollBack(AgentMessage message) {
        List<Map<String, Object>> rawMessages = memoryStore.load(sessionId, getName());
        if (rawMessages.isEmpty()) return;

        // 1. 检查最后一条消息是否是工具调用。assistant 消息
        Map<String, Object> lastMessage = rawMessages.get(rawMessages.size() - 1);
        Object toolCallsObj = lastMessage.get("tool_calls");
        if (toolCallsObj == null) return;

        List<Map<String, Object>> toolCalls;
        try {
            toolCalls = OBJECT_MAPPER.convertValue(toolCallsObj, new TypeReference<>() {});
        } catch (Exception e) {
            return;
        }
        if (toolCalls.isEmpty()) return;

        // 2. 提取工具调用信息
        Map<String, Object> toolCall = toolCalls.get(0);
        Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
        String functionName = (String) function.get("name");
        String toolCallId = (String) toolCall.get("id");

        // 3. 如果。message_ask_user，添加工具响应后继续（用户已回复。
        if ("message_ask_user".equals(functionName)) {
            try {
                Map<String, Object> toolResponse = new LinkedHashMap<>();
                toolResponse.put("role", "tool");
                toolResponse.put("tool_call_id", toolCallId);
                toolResponse.put("function_name", functionName);
                toolResponse.put("content", OBJECT_MAPPER.writeValueAsString(
                        Map.of("message", message.message(), "attachments", message.attachments())));
                rawMessages.add(toolResponse);
                memoryStore.save(sessionId, getName(), rawMessages);
            } catch (Exception e) {
                logger.error("rollBack 序列化失。 {}", e.getMessage());
            }
        } else {
            // 4. 其他工具调用：直接删除最后一。assistant（工具调用）消息
            rawMessages.remove(rawMessages.size() - 1);
            memoryStore.save(sessionId, getName(), rawMessages);
        }
    }

    /**
     * 压缩 Agent 记忆，移除重量级内容（浏览器页面等）以减。Token 消耗。
     */
    public void compactMemory() {
        memoryStore.compact(sessionId, getName());
    }

    // ===================== 消息格式转换工具方法 =====================

    /**
     * 。OpenAI 原始格式消息列表转换。Spring AI Message 列表。
     *
     * @param rawMessages OpenAI 原始格式消息
     * @return Spring AI Message 列表
     */
    protected List<Message> toSpringAiMessages(List<Map<String, Object>> rawMessages) {
        List<Message> messages = new ArrayList<>();
        for (Map<String, Object> raw : rawMessages) {
            String role = (String) raw.get("role");
            switch (role) {
                case "system" -> messages.add(new SystemMessage((String) raw.get("content")));
                case "user" -> messages.add(new UserMessage((String) raw.getOrDefault("content", "")));
                case "assistant" -> {
                    String content = (String) raw.getOrDefault("content", "");
                    Object tcObj = raw.get("tool_calls");
                    if (tcObj != null) {
                        try {
                            List<Map<String, Object>> tcList = OBJECT_MAPPER.convertValue(tcObj, new TypeReference<>() {});
                            List<AssistantMessage.ToolCall> toolCalls = tcList.stream()
                                    .map(tc -> {
                                        Map<String, Object> func = (Map<String, Object>) tc.get("function");
                                        return new AssistantMessage.ToolCall(
                                                (String) tc.get("id"),
                                                "function",
                                                (String) func.get("name"),
                                                (String) func.getOrDefault("arguments", "{}"));
                                    }).toList();
                            messages.add(AssistantMessage.builder()
                                    .content(content)
                                    .properties(Map.of())
                                    .toolCalls(toolCalls)
                                    .media(List.of())
                                    .build());
                        } catch (Exception e) {
                            messages.add(new AssistantMessage(content));
                        }
                    } else {
                        messages.add(new AssistantMessage(content));
                    }
                }
                case "tool" -> {
                    String id = (String) raw.getOrDefault("tool_call_id", "");
                    String name = (String) raw.getOrDefault("function_name", "");
                    String content = (String) raw.getOrDefault("content", "");
                    messages.add(ToolResponseMessage.builder()
                            .responses(List.of(new ToolResponseMessage.ToolResponse(id, name, content)))
                            .metadata(Map.of())
                            .build());
                }
            }
        }
        return messages;
    }

    /**
     * 。Spring AI AssistantMessage 转换。OpenAI 原始格式 Map。
     *
     * @param assistantMsg Spring AI AssistantMessage
     * @return OpenAI 原始格式 Map
     */
    protected Map<String, Object> toRawAssistantMessage(AssistantMessage assistantMsg) {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("role", "assistant");
        raw.put("content", assistantMsg.getText() != null ? assistantMsg.getText() : "");

        List<AssistantMessage.ToolCall> toolCalls = assistantMsg.getToolCalls();
        if (toolCalls != null && !toolCalls.isEmpty()) {
            List<Map<String, Object>> tcList = toolCalls.stream().map(tc -> {
                Map<String, Object> func = new LinkedHashMap<>();
                func.put("name", tc.name());
                func.put("arguments", tc.arguments());
                Map<String, Object> tcMap = new LinkedHashMap<>();
                tcMap.put("id", tc.id());
                tcMap.put("type", "function");
                tcMap.put("function", func);
                return tcMap;
            }).toList();
            raw.put("tool_calls", tcList);
        }
        return raw;
    }

    /**
     * 解析工具调用参数 JSON 字符串为 Map。
     *
     * @param argsJson JSON 格式参数字符。
     * @return 参数 Map
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseArgs(String argsJson) {
        try {
            if (argsJson == null || argsJson.isBlank()) return new HashMap<>();
            return OBJECT_MAPPER.readValue(argsJson, Map.class);
        } catch (Exception e) {
            logger.warn("解析工具参数失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 解析工具调用结果 JSON 字符串。
     *
     * @param resultJson 结果 JSON 字符。
     * @return 解析后的对象，失败时返回原始字符。
     */
    protected Object parseResult(String resultJson) {
        try {
            if (resultJson == null) return null;
            return OBJECT_MAPPER.readValue(resultJson, Object.class);
        } catch (Exception e) {
            return resultJson;
        }
    }

    /**
     * 从函数名提取工具集名称（用于 ToolEvent.toolName）。
     * 例如。shell_execute" 。"shell"。browser_navigate" 。"browser"。mcp_server_tool" 。"mcp"
     *
     * @param functionName 函数。
     * @return 工具集名。
     */
    protected String extractToolSetName(String functionName) {
        if (functionName == null) return "";
        if (functionName.startsWith("mcp_")) return "mcp";
        if (functionName.startsWith("shell_")) return "shell";
        if (functionName.startsWith("browser_")) return "browser";
        if (functionName.startsWith("search_")) return "search";
        if (functionName.startsWith("message_")) return "message";
        if (functionName.startsWith("call_remote_") || functionName.startsWith("get_remote_")) return "a2a";
        if (functionName.startsWith("shell")) return "shell";
        if (functionName.startsWith("browser")) return "browser";
        if (functionName.startsWith("search")) return "search";
        if (functionName.startsWith("message")) return "message";
        // 文件工具无前缀
        if ("read_file".equals(functionName) || "write_file".equals(functionName)
                || "replace_in_file".equals(functionName) || "search_in_file".equals(functionName)
                || "find_files".equals(functionName)) return "file";
        if ("readFile".equals(functionName) || "writeFile".equals(functionName)
                || "replaceInFile".equals(functionName) || "searchInFile".equals(functionName)
                || "findFiles".equals(functionName)) return "file";
        return functionName;
    }

    /**
     * 解析附件列表。
     */
    @SuppressWarnings("unchecked")
    protected List<String> parseAttachments(Object attachmentsObj) {
        List<String> attachments = new ArrayList<>();
        if (attachmentsObj instanceof List) {
            for (Object a : (List<?>) attachmentsObj) {
                if (a != null) attachments.add(a.toString());
            }
        }
        return attachments;
    }
}

