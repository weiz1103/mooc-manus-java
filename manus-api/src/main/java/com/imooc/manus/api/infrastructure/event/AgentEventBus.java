package com.imooc.manus.api.infrastructure.event;

import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.interfaces.sse.SsePublisher;
import com.imooc.manus.common.event.BaseEvent;
import org.springframework.stereotype.Component;

/**
 * Agent 事件总线。
 *
 * <p>统一处理每一个 Agent 产生事件的三个副作用，且顺序严格保证：
 * <ol>
 *   <li><b>持久化优先</b>：将事件写入会话历史（数据库），保证不丢失。</li>
 *   <li><b>状态同步</b>：根据事件类型更新会话投影（标题、状态、未读数）。</li>
 *   <li><b>推送最后</b>：通过 SSE 推送给前端，失败不影响前两步。</li>
 * </ol>
 * </p>
 *
 * <p>新增事件类型时，只需在 {@link EventPersister} 和 {@link SessionStateSync} 中
 * 扩展对应逻辑，无需修改 Controller 或 Service。</p>
 */
@Component
public class AgentEventBus {

    private final EventPersister eventPersister;
    private final SessionStateSync sessionStateSync;

    public AgentEventBus(EventPersister eventPersister, SessionStateSync sessionStateSync) {
        this.eventPersister = eventPersister;
        this.sessionStateSync = sessionStateSync;
    }

    /**
     * 分发一个 Agent 事件。
     *
     * <p>当前顺序为：先落会话历史与 task_event_log，再同步会话投影，最后推送 SSE。
     * 这样可以保证数据库里的事件账本与前端看到的事件语义一致；
     * 同时，{@code AgentRunner} 在调用本方法前已经写入 Redis Stream，
     * 即使当前连接断开，客户端仍可基于 event id 续流。</p>
     *
     * @param session   当前会话（状态同步时会修改内存对象状态）
     * @param event     要分发的事件
     * @param publisher SSE 推送器（可为 null，表示不推送）
     */
    public void dispatch(Session session, BaseEvent event, SsePublisher publisher) {
        // Step 1: 持久化到 DB（事件历史 + task_event_log，用于回放与审计）
        eventPersister.save(session, event);

        // Step 2: 更新会话投影（title / status / unreadCount 等）
        sessionStateSync.sync(session, event);

        // Step 3: 最后推送 SSE，避免“前端已看到但账本未落成”的不一致
        if (publisher != null && !publisher.isClientGone()) {
            publisher.publish(event);
        }
    }
}
