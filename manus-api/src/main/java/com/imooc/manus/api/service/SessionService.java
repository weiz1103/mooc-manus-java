package com.imooc.manus.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.event.AgentEventBus;
import com.imooc.manus.api.domain.model.file.FileMeta;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.model.task.TaskExecution;
import com.imooc.manus.api.domain.repository.FileRepository;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.exception.SessionNotFoundException;
import com.imooc.manus.api.observability.ExecutionObservationSink;
import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.common.event.DoneEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会话管理服务。
 *
 * <p>负责会话的创建、查询、删除等生命周期管理，不涉及 Agent 执行逻辑。
 * 原 CreateSessionUseCase / DeleteSessionUseCase / GetSessionDetailUseCase 等
 * 6 个独立 UseCase 类合并至此，职责清晰且符合 Spring 开发规范。</p>
 */
@Service
@Transactional(readOnly = true)
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private final SessionRepository sessionRepository;
    private final FileRepository fileRepository;
    private final AgentTaskService agentTaskService;
    private final AgentEventBus agentEventBus;
    private final TaskExecutionService taskExecutionService;
    private final ExecutionObservationSink executionObservationSink;

    public SessionService(SessionRepository sessionRepository,
                          FileRepository fileRepository,
                          AgentTaskService agentTaskService,
                          AgentEventBus agentEventBus,
                          TaskExecutionService taskExecutionService,
                          ExecutionObservationSink executionObservationSink) {
        this.sessionRepository = sessionRepository;
        this.fileRepository = fileRepository;
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
     * 查询所有会话（按创建时间倒序）。
     */
    public List<Session> listAll() {
        return sessionRepository.getAll();
    }

    /**
     * 查询单个会话详情（含历史事件和文件列表）。
     */
    public Session getDetail(String sessionId) {
        return sessionRepository.getById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
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

    /**
     * 获取会话下的文件列表。
     */
    public List<FileMeta> listFiles(String sessionId) {
        Session session = sessionRepository.getById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        return session.getFiles() != null ? session.getFiles() : List.of();
    }
}
