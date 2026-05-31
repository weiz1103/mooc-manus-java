package com.imooc.manus.api.infrastructure.external.task;

import com.imooc.manus.api.domain.external.MessageQueue;
import com.imooc.manus.api.domain.external.Task;
import com.imooc.manus.api.infrastructure.external.messagequeue.RedisStreamMessageQueue;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于Redis Stream的任务流载体。
 * @author zhuang03@qq.com
 * @date 2026-05-29 05:00:41
 */
public class RedisStreamTask implements Task {

    private static final String INPUT_PREFIX = "task_input_";
    private static final String OUTPUT_PREFIX = "task_output_";

    private final String id;
    private final MessageQueue inputStream;
    private final MessageQueue outputStream;

    /**
     * 构造函数，完成Redis Stream任务管道的初始化
     *
     * @param id            任务id
     * @param redisTemplate Redis模板
     */
    public RedisStreamTask(String id, RedisTemplate<String, String> redisTemplate) {
        this.id = id;
        this.inputStream = new RedisStreamMessageQueue(redisTemplate, inputStreamKey(id));
        this.outputStream = new RedisStreamMessageQueue(redisTemplate, outputStreamKey(id));
    }

    public static String inputStreamKey(String taskId) {
        return INPUT_PREFIX + taskId;
    }

    public static String outputStreamKey(String taskId) {
        return OUTPUT_PREFIX + taskId;
    }

    /**
     * 只读属性，返回任务的输入流
     *
     * @return 输入流（Redis Stream）
     */
    @Override
    public MessageQueue getInputStream() {
        return inputStream;
    }

    /**
     * 只读属性，返回任务的输出流
     *
     * @return 输出流（Redis Stream）
     */
    @Override
    public MessageQueue getOutputStream() {
        return outputStream;
    }

    /**
     * 只读属性，返回任务的id
     *
     * @return 任务id
     */
    @Override
    public String getId() {
        return id;
    }
}
