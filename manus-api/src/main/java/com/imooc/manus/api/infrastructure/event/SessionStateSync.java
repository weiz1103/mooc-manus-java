package com.imooc.manus.api.infrastructure.event;

import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.common.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 会话状态同步组件。
 *
 * <p>根据 Agent 事件类型，更新会话的投影字段（标题、状态、未读数、最新消息）。
 * 与 {@link EventPersister} 分离，职责单一，便于独立测试和扩展。</p>
 *
 * <p>事件→投影映射规则：
 * <ul>
 *   <li>{@code MessageEvent(role=assistant)} → 更新最新消息 + 未读数+1</li>
 *   <li>{@code TitleEvent} → 更新会话标题</li>
 *   <li>{@code WaitEvent} → 会话状态流转为 WAITING</li>
 *   <li>{@code DoneEvent} → 会话状态流转为 COMPLETED</li>
 * </ul>
 * </p>
 */
@Component
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-26 13:07:17
 */
public class SessionStateSync {

    private static final Logger log = LoggerFactory.getLogger(SessionStateSync.class);

    private final SessionRepository sessionRepository;

    public SessionStateSync(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void sync(Session session, BaseEvent event) {
        try {
            if (event instanceof MessageEvent me && "assistant".equals(me.getRole())) {
                sessionRepository.updateLatestMessage(session.getId(), me.getMessage(), LocalDateTime.now());
                sessionRepository.incrementUnreadMessageCount(session.getId());

            } else if (event instanceof TitleEvent te) {
                sessionRepository.updateTitle(session.getId(), te.getTitle());

            } else if (event instanceof WaitEvent) {
                session.waitForInput();
                sessionRepository.updateStatus(session.getId(), session.getStatus());

            } else if (event instanceof DoneEvent) {
                session.complete();
                sessionRepository.updateStatus(session.getId(), session.getStatus());
            }
        } catch (Exception e) {
            log.warn("会话状态同步失败: sessionId={}, event={}, error={}",
                    session.getId(), event.getClass().getSimpleName(), e.getMessage());
        }
    }
}
