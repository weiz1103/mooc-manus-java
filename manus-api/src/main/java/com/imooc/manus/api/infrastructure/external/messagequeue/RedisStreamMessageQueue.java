package com.imooc.manus.api.infrastructure.external.messagequeue;

import com.imooc.manus.api.domain.external.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 基于Redis Stream的消息队列实现。
 * 对应Python中的 RedisStreamMessageQueue 类。
 *
 * @author thezehui@gmail.com
 */
public class RedisStreamMessageQueue implements MessageQueue {

    private static final Logger logger = LoggerFactory.getLogger(RedisStreamMessageQueue.class);
    private static final String FIELD = "data";

    private final RedisTemplate<String, String> redisTemplate;
    private final String streamKey;

    /**
     * 构造函数，完成Redis Stream消息队列的初始化
     *
     * @param redisTemplate Redis模板
     * @param streamKey     Stream的key（对应Python的stream_name）
     */
    public RedisStreamMessageQueue(RedisTemplate<String, String> redisTemplate, String streamKey) {
        this.redisTemplate = redisTemplate;
        this.streamKey = streamKey;
    }

    /**
     * 往消息队列中添加一条消息
     *
     * @param message 要添加的消息（JSON字符串）
     * @return 消息id
     */
    @Override
    public String put(String message) {
        StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
        RecordId recordId = ops.add(streamKey, Map.of(FIELD, message));
        return recordId != null ? recordId.getValue() : "*";
    }

    /**
     * 根据传递的开始id+阻塞时间，获取1条数据
     *
     * @param startId 开始id（可为null，从最新的开始）
     * @param blockMs 阻塞时间（毫秒），0表示非阻塞
     * @return 消息id和消息内容的数组，如果没有消息则返回null
     */
    @Override
    @SuppressWarnings("unchecked")
    public String[] get(String startId, Integer blockMs) {
        try {
            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
            String id = startId != null ? startId : "0-0";

            List<MapRecord<String, String, String>> records;
            if (blockMs != null && blockMs > 0) {
                // 阻塞读取（等同于Python的 block=blockMs）
                records = ops.read(
                        StreamReadOptions.empty().block(Duration.ofMillis(blockMs)).count(1),
                        StreamOffset.create(streamKey, ReadOffset.from(id))
                );
            } else {
                records = ops.read(
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(streamKey, ReadOffset.from(id))
                );
            }

            if (records == null || records.isEmpty()) return null;

            MapRecord<String, String, String> record = records.get(0);
            String messageId = record.getId().getValue();
            String content = record.getValue().get(FIELD);
            return new String[]{messageId, content};
        } catch (Exception e) {
            logger.error("从Redis Stream获取消息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取并移除消息队列中的第一条消息（XRANGE + XDEL）
     *
     * @return 消息id和消息内容的数组，如果没有消息则返回null
     */
    @Override
    @SuppressWarnings("unchecked")
    public String[] pop() {
        try {
            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
            List<MapRecord<String, String, String>> records = ops.read(
                    StreamReadOptions.empty().count(1),
                    StreamOffset.create(streamKey, ReadOffset.from("0-0"))
            );

            if (records == null || records.isEmpty()) return null;

            MapRecord<String, String, String> record = records.get(0);
            String messageId = record.getId().getValue();
            String content = record.getValue().get(FIELD);

            // 删除该消息
            ops.delete(streamKey, record.getId());

            return new String[]{messageId, content};
        } catch (Exception e) {
            logger.error("从Redis Stream弹出消息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 清空消息队列中的所有消息
     */
    @Override
    public void clear() {
        try {
            redisTemplate.delete(streamKey);
        } catch (Exception e) {
            logger.error("清空Redis Stream失败: {}", e.getMessage());
        }
    }

    /**
     * 判断消息队列是否为空
     *
     * @return 是否为空
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 获取消息队列的长度
     *
     * @return 队列长度
     */
    @Override
    public long size() {
        try {
            Long len = redisTemplate.opsForStream().size(streamKey);
            return len != null ? len : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据传递的消息id删除队列中指定的消息
     *
     * @param messageId 消息id
     * @return 是否删除成功
     */
    @Override
    public boolean deleteMessage(String messageId) {
        try {
            Long result = redisTemplate.opsForStream().delete(streamKey, RecordId.of(messageId));
            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("删除Redis Stream消息失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 只读属性，返回当前Stream的key
     *
     * @return Stream key
     */
    public String getStreamKey() {
        return streamKey;
    }
}

