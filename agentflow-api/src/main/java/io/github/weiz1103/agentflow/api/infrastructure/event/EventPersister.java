package io.github.weiz1103.agentflow.api.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.weiz1103.agentflow.api.domain.external.Sandbox;
import io.github.weiz1103.agentflow.api.domain.model.file.FileMeta;
import io.github.weiz1103.agentflow.api.domain.model.session.Session;
import io.github.weiz1103.agentflow.api.domain.model.task.TaskEventLog;
import io.github.weiz1103.agentflow.api.domain.model.toolresult.ToolResult;
import io.github.weiz1103.agentflow.api.domain.repository.SessionRepository;
import io.github.weiz1103.agentflow.api.domain.repository.TaskEventLogRepository;
import io.github.weiz1103.agentflow.api.infrastructure.config.RedisTaskFactory;
import io.github.weiz1103.agentflow.common.event.BaseEvent;
import io.github.weiz1103.agentflow.common.event.MessageEvent;
import io.github.weiz1103.agentflow.common.event.ToolEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 事件持久化组件。
 *
 * <p>负责。Agent 事件写入会话历史（数据库），并解析工具调用产生的文件元信息。
 * 使用 {@code @Transactional} 保证事件和文件在同一事务中原子写入。</p>
 */
@Component
public class EventPersister {

    private static final Logger log = LoggerFactory.getLogger(EventPersister.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private final SessionRepository sessionRepository;
    private final RedisTaskFactory taskFactory;
    private final TaskEventLogRepository taskEventLogRepository;

    public EventPersister(SessionRepository sessionRepository,
                          RedisTaskFactory taskFactory,
                          TaskEventLogRepository taskEventLogRepository) {
        this.sessionRepository = sessionRepository;
        this.taskFactory = taskFactory;
        this.taskEventLogRepository = taskEventLogRepository;
    }

    @Transactional
    public void save(Session session, BaseEvent event) {
        try {
            // 直接。common.event.BaseEvent 序列化为 Map，跳。domain.model.event 中间层。
            // 原因：common.event.MessageEvent.attachments 。List<String>。
            //       。domain.model.event.MessageEvent.attachments 。List<FileMeta>。
            //       直接 convertValue 会报 "no String-argument constructor" 类型不匹配错误。
            Map<String, Object> eventData = JSON.convertValue(event, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            sessionRepository.addEvent(session.getId(), event);
            appendTaskEventLog(session, event, eventData);

            // 解析事件中关联的文件，一并持久化
            resolveFiles(session, event).forEach(file ->
                    sessionRepository.addFile(session.getId(), file));

        } catch (Exception e) {
            log.warn("事件持久化失。 sessionId={}, eventType={}, error={}",
                    session.getId(), event.getType(), e.getMessage());
        }
    }

    private List<FileMeta> resolveFiles(Session session, BaseEvent event) {
        Set<String> paths = new LinkedHashSet<>();

        if (event instanceof MessageEvent me && me.getAttachments() != null) {
            me.getAttachments().stream()
                    .filter(p -> p != null && !p.isBlank())
                    .forEach(paths::add);
        }

        if (event instanceof ToolEvent te && te.getStatus() == ToolEvent.ToolEventStatus.CALLED) {
            if ("write_file".equals(te.getFunctionName()) || "replace_in_file".equals(te.getFunctionName())) {
                extractFilepath(te.getFunctionArgs()).ifPresent(paths::add);
            }
        }

        return paths.stream()
                .map(path -> enrichFileMeta(session, path))
                .filter(Objects::nonNull)
                .toList();
    }

    private java.util.Optional<String> extractFilepath(Map<String, Object> args) {
        if (args == null) return java.util.Optional.empty();
        for (String key : new String[]{"filepath", "path", "pathname"}) {
            if (args.get(key) instanceof String v && !v.isBlank()) {
                return java.util.Optional.of(v);
            }
        }
        return java.util.Optional.empty();
    }

    private void appendTaskEventLog(Session session, BaseEvent event, Map<String, Object> eventData) {
        if (session == null || session.getTaskId() == null || session.getTaskId().isBlank()) {
            return;
        }
        taskEventLogRepository.appendIfAbsent(TaskEventLog.of(
                session.getId(),
                session.getTaskId(),
                event != null ? event.getId() : null,
                event != null ? event.getType() : "unknown",
                eventData
        ));
    }

    /**
     * 尝试从沙箱获取文件的完整元信息（大小、扩展名等），降级返回基础 FileMeta。
     */
    private FileMeta enrichFileMeta(Session session, String filepath) {
        FileMeta fallback = FileMeta.ofFilepath(filepath);
        if (session.getSandboxId() == null) return fallback;

        try {
            Sandbox sandbox = taskFactory.getSandbox(session.getSandboxId()).orElse(null);
            if (sandbox == null) return fallback;

            String dir = parentDir(filepath);
            ToolResult<Object> result = sandbox.listFiles(dir);
            if (!result.success() || !(result.data() instanceof List<?> items)) return fallback;

            String normalizedTarget = normalize(filepath);
            for (Object item : items) {
                if (!(item instanceof Map<?, ?> map)) continue;
                Object pathVal = map.get("path");
                Object nameVal = map.get("name");
                Object extVal  = map.get("extension");
                Object sizeVal = map.get("size");
                String candidatePath = pathVal != null ? String.valueOf(pathVal) : filepath;
                String candidateName = nameVal != null ? String.valueOf(nameVal) : fallback.filename();
                if (!normalizedTarget.equalsIgnoreCase(normalize(candidatePath))
                        && !fallback.filename().equalsIgnoreCase(candidateName)) {
                    continue;
                }
                String ext = extVal != null ? String.valueOf(extVal) : fallback.extension();
                if (ext != null && ext.startsWith(".")) ext = ext.substring(1);
                long size = sizeVal instanceof Number n ? n.longValue() : fallback.size();
                return new FileMeta(fallback.id(), candidateName, candidatePath,
                        fallback.key(), ext, fallback.mimeType(), size);
            }
        } catch (Exception e) {
            log.debug("文件元信息解析失。 filepath={}", filepath);
        }
        return fallback;
    }

    private String parentDir(String path) {
        String n = normalize(path);
        int idx = n.lastIndexOf('/');
        return idx <= 0 ? "." : n.substring(0, idx);
    }

    private String normalize(String path) {
        return path == null ? "" : path.replace('\\', '/');
    }
}

