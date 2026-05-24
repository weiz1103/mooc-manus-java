package com.imooc.manus.api.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.domain.model.file.FileMeta;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.interfaces.sse.SsePublisher;
import com.imooc.manus.api.observability.ExecutionObservationSink;
import com.imooc.manus.api.service.ChatService;
import com.imooc.manus.api.service.SessionService;
import com.imooc.manus.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话接口层。
 *
 * <p>职责：HTTP 请求解析、参数组装、路由到对应 Service，以及响应格式适配。
 * 不包含任何业务逻辑，不直接操作数据库或事件流。</p>
 *
 * <p>SSE 接口使用 Java 21 虚拟线程（{@code Thread.ofVirtual()}）处理阻塞流程，
 * 避免占用 Tomcat 线程池，适合长连接场景。</p>
 */
@RestController
@RequestMapping({"/api/sessions", "/api/v1/sessions"})
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);

    private final SessionService sessionService;
    private final ChatService chatService;
    private final ExecutionObservationSink executionObservationSink;
    private final ObjectMapper objectMapper;

    public SessionController(SessionService sessionService,
                             ChatService chatService,
                             ExecutionObservationSink executionObservationSink,
                             ObjectMapper objectMapper) {
        this.sessionService = sessionService;
        this.chatService = chatService;
        this.executionObservationSink = executionObservationSink;
        this.objectMapper = objectMapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 会话 CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSession() {
        Session session = sessionService.create();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("session_id", session.getId()), "创建任务会话成功"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listSessions() {
        List<Map<String, Object>> items = sessionService.listAll().stream()
                .map(this::toSessionItem).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("sessions", items), "获取任务会话列表成功"));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(@PathVariable String sessionId) {
        Session session = sessionService.getDetail(sessionId);
        return ResponseEntity.ok(ApiResponse.success(toSessionDetail(session), "获取会话详情成功"));
    }

    @PostMapping("/{sessionId}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable String sessionId) {
        sessionService.delete(sessionId);
        return ResponseEntity.ok(ApiResponse.success("删除任务会话成功"));
    }

    @PostMapping("/{sessionId}/stop")
    public ResponseEntity<ApiResponse<Void>> stopSession(@PathVariable String sessionId) {
        sessionService.stop(sessionId);
        return ResponseEntity.ok(ApiResponse.success("停止任务会话成功"));
    }

    @PostMapping("/{sessionId}/clear-unread-message-count")
    public ResponseEntity<ApiResponse<Void>> clearUnread(@PathVariable String sessionId) {
        sessionService.clearUnreadCount(sessionId);
        return ResponseEntity.ok(ApiResponse.success("清除未读消息数成功"));
    }

    @GetMapping("/{sessionId}/files")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listFiles(@PathVariable String sessionId) {
        List<Map<String, Object>> files = sessionService.listFiles(sessionId).stream()
                .map(this::toFileItem).toList();
        return ResponseEntity.ok(ApiResponse.success(Map.of("files", files), "获取文件列表成功"));
    }

    @GetMapping("/{sessionId}/runtime-metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRuntimeMetrics(@PathVariable String sessionId) {
        Session session = sessionService.getDetail(sessionId);
        Map<String, Object> payload = executionObservationSink.getLatestBySessionId(sessionId)
                .<Map<String, Object>>map(snapshot -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("session_id", sessionId);
                    map.put("task_id", snapshot.taskId());
                    map.put("status", snapshot.status());
                    map.put("event_count", snapshot.eventCount());
                    map.put("tool_call_count", snapshot.toolCallCount());
                    map.put("step_start_count", snapshot.stepStartCount());
                    map.put("repeated_tool_call_count", snapshot.repeatedToolCallCount());
                    map.put("loop_detected", snapshot.loopDetected());
                    map.put("terminal_event", snapshot.terminalEvent() != null ? snapshot.terminalEvent() : "");
                    map.put("error_message", snapshot.errorMessage() != null ? snapshot.errorMessage() : "");
                    map.put("duration_ms", snapshot.durationMs());
                    map.put("warnings", snapshot.warnings());
                    return map;
                })
                .orElseGet(() -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("session_id", sessionId);
                    map.put("task_id", session.getTaskId() != null ? session.getTaskId() : "");
                    map.put("status", "unknown");
                    map.put("event_count", 0);
                    map.put("tool_call_count", 0);
                    map.put("step_start_count", 0);
                    map.put("repeated_tool_call_count", 0);
                    map.put("loop_detected", false);
                    map.put("terminal_event", "");
                    map.put("error_message", "");
                    map.put("duration_ms", 0);
                    map.put("warnings", List.of());
                    return map;
                });
        return ResponseEntity.ok(ApiResponse.success(payload, "获取运行观测指标成功"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 聊天 SSE 接口
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 聊天接口（SSE 流式推送）。
     *
     * <p>两种场景共用此接口：
     * <ul>
     *   <li>携带 {@code message}：发送新消息，触发 Agent 执行</li>
     *   <li>不携带 {@code message}：断线续流，从 {@code event_id} 断点重放</li>
     * </ul>
     * </p>
     */
    @PostMapping(path = "/{sessionId}/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@PathVariable String sessionId,
                           @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> req = body != null ? body : Map.of();
        String message = (String) req.get("message");
        String eventId = (String) req.get("event_id");
        @SuppressWarnings("unchecked")
        List<String> attachments = (List<String>) req.getOrDefault("attachments", List.of());
        LocalDateTime timestamp = parseTimestamp(req.get("timestamp"));

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        SsePublisher publisher = new SsePublisher(emitter, objectMapper);

        // 虚拟线程处理阻塞的 AI 执行过程，不阻塞 Tomcat 线程
        Thread.ofVirtual().name("chat-" + sessionId).start(() -> {
            try {
                chatService.chat(sessionId, message, attachments, eventId, timestamp, publisher);
            } finally {
                publisher.complete();
            }
        });

        return emitter;
    }

    /**
     * 会话列表 SSE 流（每 5 秒推送一次最新列表）。
     */
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSessions() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        SsePublisher publisher = new SsePublisher(emitter, objectMapper);

        Thread.ofVirtual().name("session-stream").start(() -> {
            while (!publisher.isClientGone()) {
                try {
                    List<Map<String, Object>> items = sessionService.listAll().stream()
                            .map(this::toSessionItem).toList();
                    // 此处直接调用 emitter（list stream 无需走 AgentEventBus）
                    emitter.send(SseEmitter.event().name("sessions")
                            .data(Map.of("sessions", items)));
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    break;
                }
            }
            publisher.complete();
        });

        return emitter;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO 组装（将领域对象映射为前端契约字段）
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, Object> toSessionItem(Session s) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("session_id", s.getId());
        item.put("title", s.getTitle());
        item.put("latest_message", s.getLatestMessage());
        item.put("latest_message_at", s.getLatestMessageAt());
        item.put("status", s.getStatus() != null ? s.getStatus().getValue() : "pending");
        item.put("unread_message_count", s.getUnreadMessageCount());
        return item;
    }

    private Map<String, Object> toSessionDetail(Session s) {
        Map<String, Object> detail = toSessionItem(s);
        detail.put("events", s.getEvents());
        detail.put("files", s.getFiles());
        return detail;
    }

    private Map<String, Object> toFileItem(FileMeta f) {
        return Map.of(
                "id", f.id(), "filename", f.filename(), "filepath", f.filepath(),
                "key", f.key(), "extension", f.extension(),
                "content_type", f.mimeType(), "size", f.size());
    }

    private LocalDateTime parseTimestamp(Object raw) {
        if (raw instanceof Number n) {
            return LocalDateTime.ofEpochSecond(n.longValue(), 0, ZoneOffset.UTC);
        }
        return null;
    }
}
