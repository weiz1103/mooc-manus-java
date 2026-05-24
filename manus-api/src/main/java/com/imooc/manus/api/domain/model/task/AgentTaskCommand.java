package com.imooc.manus.api.domain.model.task;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台 Worker 执行的任务提交命令。
 *
 * <p>该对象是运行时内部模型，不对前端暴露，专门用于将一次用户消息转化为
 * 可异步执行的任务载荷。</p>
 */
public record AgentTaskCommand(
        String sessionId,
        String taskId,
        String message,
        List<String> attachments,
        LocalDateTime timestamp
) {
    public AgentTaskCommand {
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
}

