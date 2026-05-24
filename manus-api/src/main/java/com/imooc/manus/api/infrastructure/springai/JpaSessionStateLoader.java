package com.imooc.manus.api.infrastructure.springai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.domain.model.plan.Plan;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.model.session.SessionStatus;
import com.imooc.manus.api.domain.repository.IUnitOfWork;
import com.imooc.manus.springai.session.SessionStateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * 基于 JPA（数据库）的 SessionStateLoader 实现。
 * 对应Python中通过 IUnitOfWork.session 加载和更新会话状态的操作。
 *
 * @author thezehui@gmail.com
 */
public class JpaSessionStateLoader implements SessionStateLoader {

    private static final Logger logger = LoggerFactory.getLogger(JpaSessionStateLoader.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    /** UoW 工厂 */
    private final Supplier<IUnitOfWork> uowFactory;

    /**
     * 构造函数
     *
     * @param uowFactory UoW 工厂
     */
    public JpaSessionStateLoader(Supplier<IUnitOfWork> uowFactory) {
        this.uowFactory = uowFactory;
    }

    @Override
    public String getSessionStatus(String sessionId) {
        try (IUnitOfWork uow = uowFactory.get()) {
            Session session = uow.session().getById(sessionId)
                    .orElse(null);
            uow.commit();
            if (session == null) return "PENDING";
            return session.getStatus() != null ? session.getStatus().name() : "PENDING";
        } catch (Exception e) {
            logger.error("获取会话[{}]状态失败: {}", sessionId, e.getMessage());
            return "PENDING";
        }
    }

    @Override
    public String getLatestPlanJson(String sessionId) {
        try (IUnitOfWork uow = uowFactory.get()) {
            Session session = uow.session().getById(sessionId).orElse(null);
            uow.commit();
            if (session == null) return null;
            // 获取 manus-api 的 Plan 并序列化为 JSON
            // manus-spring-ai 的 Plan 字段与 manus-api Plan 字段兼容（相同 JSON 格式）
            Plan plan = session.getLatestPlan().orElse(null);
            if (plan == null) return null;
            return OBJECT_MAPPER.writeValueAsString(plan);
        } catch (Exception e) {
            logger.error("获取会话[{}]最新规划失败: {}", sessionId, e.getMessage());
            return null;
        }
    }

    @Override
    public void updateSessionStatus(String sessionId, String status) {
        try (IUnitOfWork uow = uowFactory.get()) {
            SessionStatus sessionStatus = SessionStatus.valueOf(status);
            uow.session().updateStatus(sessionId, sessionStatus);
            uow.commit();
        } catch (Exception e) {
            logger.error("更新会话[{}]状态失败: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void savePlanJson(String sessionId, String planJson) {
        // Plan 已通过 addEvent(PlanEvent) 保存，此处暂不额外持久化
        // 如需额外持久化 Plan，可扩展此方法
        logger.debug("会话[{}] Plan 已通过事件流持久化，savePlanJson 暂不单独处理", sessionId);
    }
}

