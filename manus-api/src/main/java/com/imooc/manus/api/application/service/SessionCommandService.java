package com.imooc.manus.api.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.infrastructure.event.AgentEventBus;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.model.task.TaskExecution;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.domain.exception.SessionNotFoundException;
import com.imooc.manus.api.infrastructure.observability.ExecutionObservationSink;
import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.common.event.DoneEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话命令服务 (CQRS Write)
 * 仅处理会话生命周期的变更操作（创建、删除、停止）。
 */
@Service
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 01:49:20
 */
public class SessionCommandService {

    private static final Logger log = LoggerFactory.getLogger(SessionCommandService.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private final SessionRepository sessionRepository;
    private final AgentTaskService agentTaskService;
    private final AgentEventBus agentEventBus;
    private final TaskExecutionService taskExecutionService;
    private final ExecutionObservationSink executionObservationSink;

    public SessionCommandService(SessionRepository sessionRepository,
                                 AgentTaskService agentTaskService,
                                 AgentEventBus agentEventBus,
                                 TaskExecutionService taskExecutionService,
                                 ExecutionObservationSink executionObservationSink) {
        this.sessionRepository = sessionRepository;
        this.agentTaskService = agentTaskService;
        this.agentEventBus = agentEventBus;
        this.taskExecutionService = taskExecutionService;
        this.executionObservationSink = executionObservationSink;
    }

    /**
     * 创建新会话。
     */
    @Transactional
    public Session create() {
        Session session = new Session();
        session.setTitle("新对话");
        sessionRepository.save(session);
        log.info("新会话已创建: sessionId={}", session.getId());
        return session;
    }

    /**
     * 删除会话。
     */
    @Transactional
    public void delete(String sessionId) {
        sessionRepository.getById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        sessionRepository.deleteById(sessionId);
        log.info("会话已删除: sessionId={}", sessionId);
    }

    /**
     * 清除未读消息计数。
     */
    @Transactional
    public void clearUnreadCount(String sessionId) {
        sessionRepository.updateUnreadMessageCount(sessionId, 0);
    }

    /**
     * 停止会话（将状态设为 COMPLETED）。
     */
    @Transactional
    public void stop(String sessionId) {
        Session session = sessionRepository.getById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        TaskExecution execution = taskExecutionService.requestCancelBySession(sessionId, "user_stop").orElse(null);
        if (execution != null && execution.getTaskId() != null) {
            log.info("收到停止请求: sessionId={}, taskId={}, currentStatus={}",
                    sessionId, execution.getTaskId(), execution.getStatus());
            if (execution.getStartedAt() == null && execution.getFinishedAt() == null) {
                emitTerminalDoneIfPossible(session, execution.getTaskId());
                taskExecutionService.markCancelled(execution.getTaskId(), "user_stop");
                executionObservationSink.onTaskCancelled(sessionId, execution.getTaskId(), "user_stop");
            }
        }

        session.complete();
        sessionRepository.updateStatus(sessionId, session.getStatus());
        log.info("会话已停止: sessionId={}", sessionId);
    }

    private void emitTerminalDoneIfPossible(Session session, String taskId) {
        agentTaskService.findTask(session).ifPresent(task -> {
            DoneEvent doneEvent = DoneEvent.builder().build();
            recordToTaskStream(task, doneEvent);
            executionObservationSink.onEvent(session.getId(), taskId, doneEvent);
            agentEventBus.dispatch(session, doneEvent, null);
        });
    }

    private void recordToTaskStream(com.imooc.manus.api.domain.external.Task task, BaseEvent event) {
        try {
            String id = task.getOutputStream().put(JSON.writeValueAsString(event));
            event.setId(id);
        } catch (Exception e) {
            log.debug("停止时写入终止事件失败: {}", e.getMessage());
        }
    }
}
