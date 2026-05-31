package io.github.weiz1103.agentflow.api.infrastructure.repository.jpa;

import io.github.weiz1103.agentflow.api.infrastructure.model.TaskEventLogModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTaskEventLogRepository extends JpaRepository<TaskEventLogModel, String> {

    boolean existsByTaskIdAndEventId(String taskId, String eventId);

    List<TaskEventLogModel> findByTaskIdOrderByCreatedAtAscIdAsc(String taskId);
}


