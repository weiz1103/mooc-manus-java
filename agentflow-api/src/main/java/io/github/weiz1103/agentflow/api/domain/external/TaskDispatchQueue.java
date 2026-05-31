package io.github.weiz1103.agentflow.api.domain.external;

import io.github.weiz1103.agentflow.api.domain.model.task.AgentTaskCommand;

import java.time.Duration;
import java.util.List;

/**
 * Agent 后台任务分发队列。
 *
 * <p>。Task.output/input stream 不同，此抽象用于“提交任务给 worker 消费”，
 * 解决当前 HTTP 请求线程直接执行 Agent 的问题。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-27 21:09:13
 */
public interface TaskDispatchQueue {

    /** 提交一条待执行任务命令，返回队列消。id。*/
    String submit(AgentTaskCommand command);

    /**
     * 轮询一条任务命令。
     *
     * @param consumerName 消费者名称（consumer group 内唯一。
     * @param blockMs 阻塞时间（毫秒）
     * @return 队列消息包装；为空表示当前无任务
     */
    QueuedTask poll(String consumerName, int blockMs);

    /**
     * 认领长时间未确认。pending 消息，用。worker crash 后恢复。
     */
    List<QueuedTask> claimStale(String consumerName, Duration minIdleTime, int count);

    /** 成功处理后确认删除队列消息。*/
    boolean ack(String messageId);

    /** 当前使用。consumer group 名称。*/
    String getConsumerGroup();

    record QueuedTask(String messageId, AgentTaskCommand command, boolean reclaimed) {}
}


