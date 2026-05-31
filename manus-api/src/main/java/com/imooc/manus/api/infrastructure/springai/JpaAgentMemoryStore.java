package com.imooc.manus.api.infrastructure.springai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.domain.repository.IUnitOfWork;
import com.imooc.manus.api.domain.model.memory.Memory;
import com.imooc.manus.springai.memory.AgentMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 基于 JPA（数据库）的 AgentMemoryStore 实现。
 *
 * <p>
 * 通过 IUnitOfWork 将 Agent 记忆持久化到数据库，保证会话间状态一致性。
 * 扩展 manus-spring-ai 的 AgentMemoryStore 接口，提供 DB 后端。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-31 00:27:03
 */
public class JpaAgentMemoryStore implements AgentMemoryStore {

    private static final Logger logger = LoggerFactory.getLogger(JpaAgentMemoryStore.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    /** UoW 工厂，用于获取数据库事务上下文 */
    private final Supplier<IUnitOfWork> uowFactory;

    /**
     * 构造函数
     *
     */
    public JpaAgentMemoryStore(Supplier<IUnitOfWork> uowFactory) {
        this.uowFactory = uowFactory;
    }

    @Override
    public List<Map<String, Object>> load(String sessionId, String agentName) {
        try (IUnitOfWork uow = uowFactory.get()) {
            Memory memory = uow.session().getMemory(sessionId, agentName);
            uow.commit();
            if (memory == null) return new ArrayList<>();
            return memory.getMessages() != null ? memory.getMessages() : new ArrayList<>();
        } catch (Exception e) {
            logger.error("加载 Agent[{}] 会话[{}] 记忆失败: {}", agentName, sessionId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void save(String sessionId, String agentName, List<Map<String, Object>> messages) {
        try (IUnitOfWork uow = uowFactory.get()) {
            Memory memory = uow.session().getMemory(sessionId, agentName);
            if (memory == null) memory = new Memory();
            memory.setMessages(new ArrayList<>(messages));
            uow.session().saveMemory(sessionId, agentName, memory);
            uow.commit();
        } catch (Exception e) {
            logger.error("保存 Agent[{}] 会话[{}] 记忆失败: {}", agentName, sessionId, e.getMessage());
        }
    }
}

