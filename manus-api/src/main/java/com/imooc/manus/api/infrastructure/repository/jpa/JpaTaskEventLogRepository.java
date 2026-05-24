package com.imooc.manus.api.infrastructure.repository.jpa;

import com.imooc.manus.api.infrastructure.model.TaskEventLogModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTaskEventLogRepository extends JpaRepository<TaskEventLogModel, String> {

    boolean existsByTaskIdAndEventId(String taskId, String eventId);

    List<TaskEventLogModel> findByTaskIdOrderByCreatedAtAscIdAsc(String taskId);
}

