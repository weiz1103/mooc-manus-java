package com.imooc.manus.api.interfaces.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.common.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SSE 事件推送器。
 *
 * <p>封装 {@link SseEmitter} 的发送细节，包括客户端断开检测和异常处理。
 * Controller 只需调用 {@link #publish(BaseEvent)}，无需关心底层 IO 细节。
 * 线程安全：可在虚拟线程中安全调用。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-28 22:59:51
 */
public class SsePublisher {

    private static final Logger log = LoggerFactory.getLogger(SsePublisher.class);

    private final SseEmitter emitter;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean clientGone = new AtomicBoolean(false);

    /**
     * @param emitter      Spring MVC 提供的 SSE 发射器
     * @param objectMapper Spring 容器中已配置好多态序列化的 ObjectMapper
     *                     （必须使用共享实例，保证 @JsonTypeInfo 等注解生效）
     */
    public SsePublisher(SseEmitter emitter, ObjectMapper objectMapper) {
        this.emitter = emitter;
        this.objectMapper = objectMapper;
        // 客户端主动断开时标记，避免后续无效推送
        emitter.onCompletion(() -> clientGone.set(true));
        emitter.onTimeout(() -> clientGone.set(true));
        emitter.onError(e -> clientGone.set(true));
    }

    /**
     * 向客户端推送一个 Agent 事件。
     *
     * @return false 表示客户端已断开，调用方可据此提前终止
     */
    public boolean publish(BaseEvent event) {
        if (clientGone.get()) {
            return false;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            emitter.send(SseEmitter.event()
                    .id(event.getId())
                    .name(resolveEventType(event))
                    .data(json));
            return true;
        } catch (Exception e) {
            if (isClientDisconnect(e)) {
                clientGone.set(true);
                log.debug("SSE 客户端已断开");
                return false;
            }
            log.warn("SSE 推送事件失败: type={}, error={}", resolveEventType(event), e.getMessage());
            return true; // 推送失败但客户端可能还在，不中断流程
        }
    }

    public void complete() {
        if (!clientGone.get()) {
            emitter.complete();
        }
    }

    public boolean isClientGone() {
        return clientGone.get();
    }

    private String resolveEventType(BaseEvent event) {
        if (event.getType() != null) return event.getType();
        return event.getClass().getSimpleName()
                .replace("Event", "").toLowerCase();
    }

    /**
     * 判断异常是否由客户端断开引起。
     * 兼容 Tomcat（ClientAbortException）和 Spring WebMVC（AsyncRequestNotUsableException）。
     */
    private boolean isClientDisconnect(Throwable t) {
        while (t != null) {
            String name = t.getClass().getName();
            String msg = t.getMessage();
            if (name.contains("ClientAbortException")
                    || name.contains("AsyncRequestNotUsableException")
                    || (msg != null && (msg.contains("Broken pipe") || msg.contains("Connection reset")))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
