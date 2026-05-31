package io.github.weiz1103.agentflow.api.infrastructure.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.weiz1103.agentflow.common.event.BaseEvent;
import io.github.weiz1103.agentflow.api.domain.model.file.FileMeta;
import io.github.weiz1103.agentflow.api.domain.model.memory.Memory;
import io.github.weiz1103.agentflow.api.domain.model.session.Session;
import io.github.weiz1103.agentflow.api.domain.model.session.SessionStatus;
import io.github.weiz1103.agentflow.api.domain.repository.SessionRepository;
import io.github.weiz1103.agentflow.api.infrastructure.model.SessionModel;
import io.github.weiz1103.agentflow.api.infrastructure.repository.jpa.JpaSessionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于JPA的会话仓库实现。
 * @author zhuang03@qq.com
 * @date 2026-05-25 01:18:08
 */
public class JpaSessionRepositoryImpl implements SessionRepository {

    private static final Logger logger = LoggerFactory.getLogger(JpaSessionRepositoryImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules();

    private final JpaSessionRepository jpaRepo;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaSessionRepositoryImpl(JpaSessionRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void save(Session session) {
        SessionModel model = toModel(session);
        // 清洗数据，防。Postgres 不支持的 \u0000 字符导致崩溃
        model.setEvents(sanitize(model.getEvents()));
        model.setMemories(sanitize(model.getMemories()));
        model.setLatestMessage(sanitizeString(model.getLatestMessage()));
        
        model.setUpdatedAt(LocalDateTime.now());
        jpaRepo.save(model);
    }

    @Override
    public List<Session> getAll() {
        return jpaRepo.findAll().stream()
                .map(this::toDomain)
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Session> getById(String sessionId) {
        return jpaRepo.findById(sessionId).map(this::toDomain);
    }

    @Override
    public void deleteById(String sessionId) {
        jpaRepo.deleteById(sessionId);
    }

    @Override
    @Transactional
    public void updateTitle(String sessionId, String title) {
        jpaRepo.updateTitle(sessionId, sanitizeString(title));
    }

    @Override
    @Transactional
    public void updateLatestMessage(String sessionId, String message, LocalDateTime timestamp) {
        jpaRepo.updateLatestMessage(sessionId, sanitizeString(message), timestamp);
    }

    @Override
    @Transactional
    public void updateUnreadMessageCount(String sessionId, int count) {
        jpaRepo.updateUnreadMessageCount(sessionId, count);
    }

    @Override
    @Transactional
    public void incrementUnreadMessageCount(String sessionId) {
        Optional<SessionModel> model = jpaRepo.findById(sessionId);
        model.ifPresent(m -> {
            m.setUnreadMessageCount(m.getUnreadMessageCount() + 1);
            m.setUpdatedAt(LocalDateTime.now());
            jpaRepo.save(m);
        });
    }

    @Override
    @Transactional
    public void decrementUnreadMessageCount(String sessionId) {
        Optional<SessionModel> model = jpaRepo.findById(sessionId);
        model.ifPresent(m -> {
            int count = Math.max(0, m.getUnreadMessageCount() - 1);
            m.setUnreadMessageCount(count);
            m.setUpdatedAt(LocalDateTime.now());
            jpaRepo.save(m);
        });
    }

    @Override
    @Transactional
    public void updateStatus(String sessionId, SessionStatus status) {
        jpaRepo.updateStatus(sessionId, status.getDatabaseValue());
    }

    @Override
    @Transactional
    public void addEvent(String sessionId, BaseEvent event) {
        jpaRepo.findById(sessionId).ifPresent(model -> {
            List<Map<String, Object>> events = model.getEvents();
            if (events == null) events = new ArrayList<>();
            try {
                Map<String, Object> eventMap = OBJECT_MAPPER.convertValue(event, new TypeReference<>() {});
                events.add(sanitize(eventMap)); // 清洗单个事件数据
            } catch (Exception e) {
                logger.error("序列化事件失。 {}", e.getMessage());
            }
            model.setEvents(events);
            model.setUpdatedAt(LocalDateTime.now());
            jpaRepo.save(model);
        });
    }

    @Override
    @Transactional
    public void addFile(String sessionId, FileMeta file) {
        jpaRepo.findById(sessionId).ifPresent(model -> {
            List<Map<String, Object>> files = model.getFiles();
            if (files == null) files = new ArrayList<>();
            try {
                Map<String, Object> fileMap = OBJECT_MAPPER.convertValue(file, new TypeReference<>() {});
                // 移除旧的同路径文。
                String filepath = file.filepath();
                files.removeIf(f -> filepath != null && filepath.equals(f.get("filepath")));
                files.add(fileMap);
            } catch (Exception e) {
                logger.error("序列化文件信息失。 {}", e.getMessage());
            }
            model.setFiles(files);
            model.setUpdatedAt(LocalDateTime.now());
            jpaRepo.save(model);
        });
    }

    @Override
    @Transactional
    public void removeFile(String sessionId, String fileId) {
        jpaRepo.findById(sessionId).ifPresent(model -> {
            List<Map<String, Object>> files = model.getFiles();
            if (files != null) {
                files.removeIf(f -> fileId.equals(f.get("filepath")) || fileId.equals(f.get("id")));
                model.setFiles(files);
                model.setUpdatedAt(LocalDateTime.now());
                jpaRepo.save(model);
            }
        });
    }

    @Override
    public Optional<FileMeta> getFileByPath(String sessionId, String filepath) {
        return jpaRepo.findById(sessionId).flatMap(model -> {
            if (model.getFiles() == null) return Optional.empty();
            return model.getFiles().stream()
                    .filter(f -> filepath.equals(f.get("filepath")))
                    .findFirst()
                    .map(f -> OBJECT_MAPPER.convertValue(f, FileMeta.class));
        });
    }

    @Override
    @Transactional
    public void saveMemory(String sessionId, String agentName, Memory memory) {
        jpaRepo.findById(sessionId).ifPresent(model -> {
            Map<String, Object> memories = model.getMemories();
            if (memories == null) memories = new LinkedHashMap<>();
            try {
                memories.put(agentName, sanitize(OBJECT_MAPPER.convertValue(memory, Object.class)));
            } catch (Exception e) {
                logger.error("序列化记忆失。 {}", e.getMessage());
            }
            model.setMemories(memories);
            model.setUpdatedAt(LocalDateTime.now());
            jpaRepo.save(model);
        });
    }

    @Override
    public Memory getMemory(String sessionId, String agentName) {
        return jpaRepo.findById(sessionId).map(model -> {
            if (model.getMemories() == null) return new Memory();
            Object memoryData = model.getMemories().get(agentName);
            if (memoryData == null) return new Memory();
            try {
                return OBJECT_MAPPER.convertValue(memoryData, Memory.class);
            } catch (Exception e) {
                logger.error("反序列化记忆失败: {}", e.getMessage());
                return new Memory();
            }
        }).orElse(new Memory());
    }

    /**
     * 将Session领域对象转换为JPA模型
     */
    private SessionModel toModel(Session session) {
        SessionModel model = new SessionModel();
        model.setId(session.getId());
        model.setSandboxId(session.getSandboxId());
        model.setTaskId(session.getTaskId());
        model.setTitle(session.getTitle());
        model.setUnreadMessageCount(session.getUnreadMessageCount());
        model.setLatestMessage(session.getLatestMessage());
        model.setLatestMessageAt(session.getLatestMessageAt());
        model.setStatus(session.getStatus() != null ? session.getStatus().getDatabaseValue() : SessionStatus.PENDING.getDatabaseValue());
        model.setCreatedAt(session.getCreatedAt() != null ? session.getCreatedAt() : LocalDateTime.now());
        model.setUpdatedAt(session.getUpdatedAt() != null ? session.getUpdatedAt() : LocalDateTime.now());

        // 序列化events
        try {
            List<Map<String, Object>> eventMaps = session.getEvents().stream()
                    .map(e -> OBJECT_MAPPER.convertValue(e, new TypeReference<Map<String, Object>>() {}))
                    .collect(Collectors.toList());
            model.setEvents(eventMaps);
        } catch (Exception e) {
            model.setEvents(new ArrayList<>());
        }

        // 序列化files
        try {
            List<Map<String, Object>> fileMaps = session.getFiles().stream()
                    .map(f -> OBJECT_MAPPER.convertValue(f, new TypeReference<Map<String, Object>>() {}))
                    .collect(Collectors.toList());
            model.setFiles(fileMaps);
        } catch (Exception e) {
            model.setFiles(new ArrayList<>());
        }

        // 序列化memories
        try {
            Map<String, Object> memoriesMap = new LinkedHashMap<>();
            for (Map.Entry<String, Memory> entry : session.getMemories().entrySet()) {
                memoriesMap.put(entry.getKey(), OBJECT_MAPPER.convertValue(entry.getValue(), Object.class));
            }
            model.setMemories(memoriesMap);
        } catch (Exception e) {
            model.setMemories(new LinkedHashMap<>());
        }

        return model;
    }

    /**
     * 将JPA模型转换为Session领域对象
     */
    private Session toDomain(SessionModel model) {
        Session session = new Session();
        session.setId(model.getId());
        session.setSandboxId(model.getSandboxId());
        session.setTaskId(model.getTaskId());
        session.setTitle(model.getTitle() != null ? model.getTitle() : "");
        session.setUnreadMessageCount(model.getUnreadMessageCount());
        session.setLatestMessage(model.getLatestMessage() != null ? model.getLatestMessage() : "");
        session.setLatestMessageAt(model.getLatestMessageAt());
        session.restore(SessionStatus.fromValue(model.getStatus()));
        session.setCreatedAt(model.getCreatedAt());
        session.setUpdatedAt(model.getUpdatedAt());

        // 反序列化events（简化：存储为Map列表，用于前端展示）
        try {
            List<BaseEvent> events = new ArrayList<>();
            if (model.getEvents() != null) {
                for (Map<String, Object> eventMap : model.getEvents()) {
                    // 简化处理：存储原始Map，序列化时通过Jackson反序列化
                    try {
                        String json = OBJECT_MAPPER.writeValueAsString(eventMap);
                        BaseEvent event = OBJECT_MAPPER.readValue(json, BaseEvent.class);
                        events.add(event);
                    } catch (Exception e) {
                        // 忽略反序列化失败的事。
                    }
                }
            }
            session.setEvents(events);
        } catch (Exception e) {
            session.setEvents(new ArrayList<>());
        }

        // 反序列化files
        try {
            List<FileMeta> files = new ArrayList<>();
            if (model.getFiles() != null) {
                for (Map<String, Object> fileMap : model.getFiles()) {
                    try {
                        files.add(OBJECT_MAPPER.convertValue(fileMap, FileMeta.class));
                    } catch (Exception e) {
                        // 忽略
                    }
                }
            }
            session.setFiles(files);
        } catch (Exception e) {
            session.setFiles(new ArrayList<>());
        }

        // 反序列化memories
        try {
            Map<String, Memory> memories = new LinkedHashMap<>();
            if (model.getMemories() != null) {
                for (Map.Entry<String, Object> entry : model.getMemories().entrySet()) {
                    try {
                        memories.put(entry.getKey(), OBJECT_MAPPER.convertValue(entry.getValue(), Memory.class));
                    } catch (Exception e) {
                        memories.put(entry.getKey(), new Memory());
                    }
                }
            }
            session.setMemories(memories);
        } catch (Exception e) {
            session.setMemories(new LinkedHashMap<>());
        }

        return session;
    }

    /**
     * 递归清洗数据，移。PostgreSQL 不支持的 \u0000 字符。
     */
    @SuppressWarnings("unchecked")
    private <T> T sanitize(T data) {
        if (data == null) return null;
        if (data instanceof String s) {
            return (T) sanitizeString(s);
        }
        if (data instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) data;
            Map<Object, Object> newMap = new LinkedHashMap<>(map.size());
            map.forEach((k, v) -> newMap.put(k, sanitize(v)));
            return (T) newMap;
        }
        if (data instanceof List) {
            List<Object> list = (List<Object>) data;
            return (T) list.stream().map(this::sanitize).collect(java.util.stream.Collectors.toList());
        }
        return data;
    }

    private String sanitizeString(String s) {
        if (s == null) return null;
        return s.replace("\u0000", "");
    }
}


