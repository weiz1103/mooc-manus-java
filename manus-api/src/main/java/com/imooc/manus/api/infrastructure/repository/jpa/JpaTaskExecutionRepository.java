package com.imooc.manus.api.infrastructure.repository.jpa;

import com.imooc.manus.api.infrastructure.model.TaskExecutionModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 17:16:34
 */
public interface JpaTaskExecutionRepository extends JpaRepository<TaskExecutionModel, String> {

    Optional<TaskExecutionModel> findTopBySessionIdOrderBySubmittedAtDesc(String sessionId);

    List<TaskExecutionModel> findBySessionIdOrderBySubmittedAtDesc(String sessionId);

    List<TaskExecutionModel> findByLoopDetectedTrueOrderByUpdatedAtDesc(Pageable pageable);

    long countBySessionIdAndSubmittedAtGreaterThanEqual(String sessionId, LocalDateTime submittedAfterInclusive);

    long countBySessionIdAndStatusIn(String sessionId, List<String> statuses);
}

