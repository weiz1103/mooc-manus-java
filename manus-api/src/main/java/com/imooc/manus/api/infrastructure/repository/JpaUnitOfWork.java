package com.imooc.manus.api.infrastructure.repository;

import com.imooc.manus.api.domain.repository.FileRepository;
import com.imooc.manus.api.domain.repository.IUnitOfWork;
import com.imooc.manus.api.domain.repository.SessionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * JPA实现的Unit of Work。
 * 对应Python中的 SQLAlchemyUoW 类。
 * <p>
 * 使用try-with-resources语法替代Python的 async with uow。
 * </p>
 *
 * @author thezehui@gmail.com
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

