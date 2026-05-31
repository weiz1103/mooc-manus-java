package io.github.weiz1103.agentflow.api.application.service;

import io.github.weiz1103.agentflow.api.domain.model.file.FileMeta;
import io.github.weiz1103.agentflow.api.domain.model.session.Session;
import io.github.weiz1103.agentflow.api.domain.repository.SessionRepository;
import io.github.weiz1103.agentflow.api.domain.exception.SessionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会话查询服务 (CQRS Read)
 * 仅处理对会话和相关文件的查询操作，提供高并发下的读扩展性。
 */
@Service
@Transactional(readOnly = true)
public class SessionQueryService {

    private final SessionRepository sessionRepository;

    public SessionQueryService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
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
     * 获取会话下的文件列表。
     */
    public List<FileMeta> listFiles(String sessionId) {
        Session session = sessionRepository.getById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        return session.getFiles() != null ? session.getFiles() : List.of();
    }
}

