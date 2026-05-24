package com.imooc.manus.api.infrastructure.config;

import com.imooc.manus.api.domain.external.Sandbox;
import com.imooc.manus.api.infrastructure.external.sandbox.DockerSandbox;
import com.imooc.manus.api.infrastructure.external.task.RedisStreamTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Redis任务工厂，用于创建和管理RedisStreamTask实例。
 *
 * @author thezehui@gmail.com
 */
public class RedisTaskFactory {

    private static final Logger logger = LoggerFactory.getLogger(RedisTaskFactory.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    public RedisTaskFactory(
            RedisTemplate<String, String> redisTemplate,
            AppProperties appProperties) {
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
    }

    /**
     * 根据传递的会话id+沙箱创建一个新任务
     *
     * @param sessionId 会话id
     * @param sandbox   沙箱实例
     * @return 创建的任务
     */
    public RedisStreamTask createTask(String sessionId, Sandbox sandbox) {
        String taskId = UUID.randomUUID().toString();

        // 创建RedisStreamTask（仅作为输入/输出流管道）
        RedisStreamTask task = new RedisStreamTask(taskId, redisTemplate);
        logger.info("创建任务成功, taskId: {}, sessionId: {}", taskId, sessionId);
        return task;
    }

    /**
     * 根据任务id获取任务实例
     *
     * @param taskId 任务id
     * @return 任务实例（Optional）
     */
    public Optional<RedisStreamTask> getTask(String taskId) {
        if (taskId == null || taskId.isBlank()) return Optional.empty();
        return Optional.of(new RedisStreamTask(taskId, redisTemplate));
    }

    public String outputStreamKey(String taskId) {
        return RedisStreamTask.outputStreamKey(taskId);
    }

    /**
     * 根据会话id创建沙箱
     *
     * @return 新沙箱实例
     */
    public Sandbox createSandbox() {
        return DockerSandbox.createFromConfig(appProperties);
    }

    /**
     * 根据沙箱id获取沙箱
     *
     * @param sandboxId 沙箱id
     * @return 沙箱实例（Optional）
     */
    public Optional<Sandbox> getSandbox(String sandboxId) {
        if (sandboxId == null || sandboxId.isBlank()) return Optional.empty();
        return DockerSandbox.getById(sandboxId, appProperties).map(s -> s);
    }

    /**
     * 销毁所有任务并释放资源
     */
    public void destroyAll() {
        logger.info("RedisTaskFactory 无本地任务缓存，无需清理");
    }
}

