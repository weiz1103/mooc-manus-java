package com.imooc.manus.api.domain.model.memory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 记忆类，定义Agent的记忆基础信息。
 * <p>
 * messages字段使用 List&lt;Map&lt;String, Object&gt;&gt; 存储，
 * 与OpenAI API的消息格式完全对应。
 * </p>
 *

 */
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-26 18:13:00
 */
public class Memory {

    private static final Logger logger = LoggerFactory.getLogger(Memory.class);

    /** 消息列表（OpenAI API格式：role, content, tool_calls等） */
    @JsonProperty("messages")
    private List<Map<String, Object>> messages;

    /** 默认构造 */
    public Memory() {
        this.messages = new ArrayList<>();
    }

    /**
     * 根据传递的消息来获取消息的角色信息
     *
     * @param message 消息Map
     * @return 消息角色字符串（user/assistant/system/tool）
     */
    public static String getMessageRole(Map<String, Object> message) {
        return (String) message.get("role");
    }

    /**
     * 往记忆中添加一条消息
     *
     * @param message 要添加的消息
     */
    public void addMessage(Map<String, Object> message) {
        this.messages.add(message);
    }

    /**
     * 往记忆中添加多条消息
     *
     * @param messages 要添加的消息列表
     */
    public void addMessages(List<Map<String, Object>> messages) {
        this.messages.addAll(messages);
    }

    /**
     * 获取记忆中的所有消息列表
     *
     * @return 消息列表
     */
    public List<Map<String, Object>> getMessages() {
        return messages;
    }

    /**
     * 获取记忆中的最后一条消息，如果不存在则返回empty
     *
     * @return 最后一条消息（Optional）
     */
    public Optional<Map<String, Object>> getLastMessage() {
        if (messages.isEmpty()) return Optional.empty();
        return Optional.of(messages.get(messages.size() - 1));
    }

    /**
     * 回滚记忆，删除最后一条消息
     */
    public void rollBack() {
        if (!messages.isEmpty()) {
            messages = new ArrayList<>(messages.subList(0, messages.size() - 1));
        }
    }

    /**
     * 记忆压缩，将记忆中已经执行的工具（搜索/网页源码获取/浏览器访问结果等）
     * 这类已经执行过的消息进行压缩检索。
     */
    public void compact() {
        // 1.循环遍历所有的消息列表
        for (Map<String, Object> message : messages) {
            // 2.判断消息的角色是否为tool
            if ("tool".equals(getMessageRole(message))) {
                String functionName = (String) message.get("function_name");
                if ("browser_view".equals(functionName) || "browser_navigate".equals(functionName)) {
                    message.put("content", "(removed)");
                    logger.debug("从记忆中移除对应工具的结果: {}", functionName);
                }
            }

            // 3.压缩记忆时reasoning_content内容可以去除压缩上下文
            if (message.containsKey("reasoning_content")) {
                String rc = String.valueOf(message.get("reasoning_content"));
                logger.debug("从记忆中移除工具思考结果: {}...", rc.length() > 50 ? rc.substring(0, 50) : rc);
                message.remove("reasoning_content");
            }
        }
    }

    /**
     * 只读属性，检查记忆是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = messages;
    }
}

