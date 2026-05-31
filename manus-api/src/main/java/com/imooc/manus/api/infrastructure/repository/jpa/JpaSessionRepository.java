package com.imooc.manus.api.infrastructure.repository.jpa;

import com.imooc.manus.api.infrastructure.model.SessionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA Repository for SessionModel.
 * @author zhuang03@qq.com
 * @date 2026-05-30 05:32:13
 */
public interface JpaSessionRepository extends JpaRepository<SessionModel, String> {

    @Modifying
    @Query("UPDATE SessionModel s SET s.title = :title, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    int updateTitle(@Param("id") String id, @Param("title") String title);

    @Modifying
    @Query("UPDATE SessionModel s SET s.status = :status, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    int updateStatus(@Param("id") String id, @Param("status") String status);

    @Modifying
    @Query("UPDATE SessionModel s SET s.unreadMessageCount = :count, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    int updateUnreadMessageCount(@Param("id") String id, @Param("count") int count);

    @Modifying
    @Query("UPDATE SessionModel s SET s.latestMessage = :msg, s.latestMessageAt = :ts, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    int updateLatestMessage(@Param("id") String id, @Param("msg") String msg, @Param("ts") java.time.LocalDateTime ts);
}

