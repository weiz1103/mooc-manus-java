package com.imooc.manus.api.domain.repository;

/**
 * UoW模式协议接口，对应Python中的 IUnitOfWork 抽象基类。
 * <p>
 * 在Java中使用AutoCloseable实现事务的自动关闭，等同于Python的 async with uow。
 * 使用try-with-resources语法替代Python上下文管理器。
 * </p>
 * <p>
 * 使用示例:
 * <pre>{@code
 *   try (var uow = uowFactory.get()) {
 *       uow.session().save(session);
 *       uow.commit();
 *   } // 自动rollback（如果未commit）
 * }</pre>
 * </p>
 *
 * @author thezehui@gmail.com
 */
public interface IUnitOfWork extends AutoCloseable {

    /** 文件仓库 */
    FileRepository file();

    /** 会话仓库 */
    SessionRepository session();

    /**
     * 提交数据库数据持久化
     */
    void commit();

    /**
     * 数据库回滚
     */
    void rollback();

    /**
     * 关闭并释放资源（实现AutoCloseable，等同于Python的 __aexit__）
     */
    @Override
    void close();
}

