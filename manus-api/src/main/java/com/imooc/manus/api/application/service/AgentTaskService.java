package com.imooc.manus.api.application.service;

import com.imooc.manus.api.domain.external.Task;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.infrastructure.config.RedisTaskFactory;
import com.imooc.manus.api.infrastructure.external.task.RedisStreamTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Agent 任务服务。
 *
 * <p>管理 Agent 执行任务（Task）和沙箱（Sandbox）的生命周期。
 * Task 是 Agent 和前端之间的事件中转通道，基于 Redis Stream 实现。</p>
 *
 * <p>职责边界：此类属于应用服务层，可以持有基础设施依赖（RedisTaskFactory），
 * 但对外只暴露领域接口 {@link Task}，屏蔽 Redis 实现细节。</p>
 */
@Service
public class AgentTaskService {

    private static final Logger log = LoggerFactory.getLogger(AgentTaskService.class);

    private final RedisTaskFactory taskFactory;
    private final SessionRepository sessionRepository;

    public AgentTaskService(RedisTaskFactory taskFactory, SessionRepository sessionRepository) {
        this.taskFactory = taskFactory;
        this.sessionRepository = sessionRepository;
    }

    /**
     * 获取会话当前关联的 Task（若存在）。
     */
    public Optional<Task> findTask(Session session) {
        if (session.getTaskId() == null) return Optional.empty();
        return taskFactory.getTask(session.getTaskId()).map(t -> t);
    }

    /**
     * 确保会话拥有一个就绪的 Task 和 Sandbox。
     *
     * <p>首次执行时创建新沙箱和新 Task；后续执行复用已有资源。
     * 方法执行后 session 的 taskId 和 sandboxId 已更新，调用方负责持久化。</p>
     *
     * @return 就绪的 Task
     */
    @Transactional
    public Task ensureTask(Session session) {
        // 复用已有沙箱，按需创建
        var sandbox = Optional.ofNullable(session.getSandboxId())
                .filter(id -> !id.isBlank())
                .flatMap(taskFactory::getSandbox)
                .orElseGet(() -> {
                    var newSandbox = taskFactory.createSandbox();
                    session.setSandboxId(newSandbox.getId());
                    log.info("沙箱已创建: sandboxId={}, sessionId={}", newSandbox.getId(), session.getId());
                    return newSandbox;
                });

        // 每轮对话创建新 Task（Redis Stream 管道）
        RedisStreamTask task = taskFactory.createTask(session.getId(), sandbox);
        session.setTaskId(task.getId());
        sessionRepository.save(session);

        log.info("Task 已就绪: taskId={}, sessionId={}", task.getId(), session.getId());
        return task;
    }
}
