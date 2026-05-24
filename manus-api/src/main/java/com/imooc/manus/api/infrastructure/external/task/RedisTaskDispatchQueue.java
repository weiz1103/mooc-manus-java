package com.imooc.manus.api.infrastructure.external.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.domain.external.TaskDispatchQueue;
import com.imooc.manus.api.domain.model.task.AgentTaskCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于 Redis Stream 的任务分发队列。
 */
public class RedisTaskDispatchQueue implements TaskDispatchQueue {

    private static final Logger log = LoggerFactory.getLogger(RedisTaskDispatchQueue.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();
    private static final String FIELD = "data";

    private final RedisTemplate<String, String> redisTemplate;
    private final String streamKey;
    private final String consumerGroup;
    private final AtomicBoolean groupInitialized = new AtomicBoolean(false);

    public RedisTaskDispatchQueue(RedisTemplate<String, String> redisTemplate, String streamKey, String consumerGroup) {
        this.redisTemplate = redisTemplate;
        this.streamKey = streamKey;
        this.consumerGroup = consumerGroup;
    }

    @Override
    public String submit(AgentTaskCommand command) {
        try {
            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
            String message = JSON.writeValueAsString(command);
            RecordId recordId = ops.add(streamKey, Map.of(FIELD, message));
            ensureConsumerGroup();
            return recordId != null ? recordId.getValue() : "*";
        } catch (Exception e) {
            throw new IllegalStateException("提交 AgentTaskCommand 到 Redis Stream 失败", e);
        }
    }

    @Override
    public QueuedTask poll(String consumerName, int blockMs) {
        ensureConsumerGroup();
        try {
            StreamReadOptions options = StreamReadOptions.empty().count(1);
            if (blockMs > 0) {
                options = options.block(Duration.ofMillis(blockMs));
            }
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                    Consumer.from(consumerGroup, consumerName),
                    options,
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );
            return records == null || records.isEmpty() ? null : toQueuedTask(records.get(0), false);
        } catch (Exception e) {
            log.error("读取任务分发流失败: streamKey={}, consumerGroup={}, consumerName={}, error={}",
                    streamKey, consumerGroup, consumerName, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<QueuedTask> claimStale(String consumerName, Duration minIdleTime, int count) {
        ensureConsumerGroup();
        if (count <= 0) {
            return List.of();
        }
        try {
            PendingMessages pending = redisTemplate.opsForStream()
                    .pending(streamKey, consumerGroup, Range.unbounded(), count);
            if (pending == null || pending.isEmpty()) {
                return List.of();
            }
            List<RecordId> staleIds = new ArrayList<>();
            for (PendingMessage message : pending) {
                if (message.getElapsedTimeSinceLastDelivery().compareTo(minIdleTime) >= 0) {
                    staleIds.add(message.getId());
                }
            }
            if (staleIds.isEmpty()) {
                return List.of();
            }
            List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream().claim(
                    streamKey,
                    consumerGroup,
                    consumerName,
                    minIdleTime,
                    staleIds.toArray(RecordId[]::new)
            );
            if (claimed == null || claimed.isEmpty()) {
                return List.of();
            }
            List<QueuedTask> result = new ArrayList<>(claimed.size());
            for (MapRecord<String, Object, Object> record : claimed) {
                QueuedTask queuedTask = toQueuedTask(record, true);
                if (queuedTask != null) {
                    result.add(queuedTask);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("认领 stale pending 任务失败: streamKey={}, consumerGroup={}, consumerName={}, error={}",
                    streamKey, consumerGroup, consumerName, e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean ack(String messageId) {
        try {
            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
            Long acked = ops.acknowledge(streamKey, consumerGroup, RecordId.of(messageId));
            ops.delete(streamKey, RecordId.of(messageId));
            return acked != null && acked > 0;
        } catch (Exception e) {
            log.error("确认任务分发消息失败: messageId={}, error={}", messageId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getConsumerGroup() {
        return consumerGroup;
    }

    private QueuedTask toQueuedTask(MapRecord<String, Object, Object> record, boolean reclaimed) {
        if (record == null) {
            return null;
        }
        try {
            AgentTaskCommand command = JSON.readValue(String.valueOf(record.getValue().get(FIELD)), AgentTaskCommand.class);
            return new QueuedTask(record.getId().getValue(), command, reclaimed);
        } catch (Exception e) {
            log.error("反序列化 AgentTaskCommand 失败, messageId={}", record.getId().getValue(), e);
            ack(record.getId().getValue());
            return null;
        }
    }

    private boolean isBusyGroupError(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains("BUSYGROUP")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void ensureConsumerGroup() {
        if (groupInitialized.get()) {
            return;
        }
        synchronized (groupInitialized) {
            if (groupInitialized.get()) {
                return;
            }
            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
            try {
                ops.createGroup(streamKey, ReadOffset.from("0-0"), consumerGroup);
                groupInitialized.set(true);
                return;
            } catch (Exception first) {
                if (isBusyGroupError(first)) {
                    groupInitialized.set(true);
                    return;
                }
                try {
                    RecordId seed = ops.add(streamKey, Map.of(FIELD, "{}"));
                    try {
                        ops.createGroup(streamKey, ReadOffset.from("0-0"), consumerGroup);
                    } catch (Exception second) {
                        if (!isBusyGroupError(second)) {
                            throw second;
                        }
                    } finally {
                        if (seed != null) {
                            ops.delete(streamKey, seed);
                        }
                    }
                    groupInitialized.set(true);
                } catch (Exception createError) {
                    throw new IllegalStateException("初始化 Redis Stream consumer group 失败", createError);
                }
            }
        }
    }
}
