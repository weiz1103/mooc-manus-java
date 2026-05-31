package io.github.weiz1103.agentflow.api.infrastructure.repository;

import io.github.weiz1103.agentflow.api.domain.repository.FileRepository;
import io.github.weiz1103.agentflow.api.domain.repository.IUnitOfWork;
import io.github.weiz1103.agentflow.api.domain.repository.SessionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * JPA实现的Unit of Work。
 * <p>
 * 使用try-with-resources语法替代Python。async with uow。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-30 18:09:13
 */
public class JpaUnitOfWork implements IUnitOfWork {

    private final EntityManager entityManager;
    private final SessionRepository sessionRepository;
    private final FileRepository fileRepository;
    private EntityTransaction transaction;

    public JpaUnitOfWork(EntityManager entityManager,
                         SessionRepository sessionRepository,
                         FileRepository fileRepository) {
        this.entityManager = entityManager;
        this.sessionRepository = sessionRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    public FileRepository file() {
        return fileRepository;
    }

    @Override
    public SessionRepository session() {
        return sessionRepository;
    }

    @Override
    public void commit() {
        // 由Spring的@Transactional管理，这里无需额外commit
        if (entityManager != null && entityManager.isJoinedToTransaction()) {
            entityManager.flush();
        }
    }

    @Override
    public void rollback() {
        // Spring事务管理
    }

    @Override
    public void close() {
        // Spring容器管理EntityManager生命周期，无需手动关闭
    }
}


