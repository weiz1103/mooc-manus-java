package io.github.weiz1103.agentflow.api.application.service;

import io.github.weiz1103.agentflow.api.domain.model.task.TaskExecution;
import io.github.weiz1103.agentflow.api.domain.model.task.TaskExecutionStatus;
import io.github.weiz1103.agentflow.api.domain.repository.TaskEventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 离线评估与回放服务。
 */
@Service
public class TaskEvaluationService {

    private final TaskExecutionService taskExecutionService;
    private final TaskEventLogRepository taskEventLogRepository;

    public TaskEvaluationService(TaskExecutionService taskExecutionService) {
        this(taskExecutionService, null);
    }

    @Autowired
    public TaskEvaluationService(TaskExecutionService taskExecutionService,
                                 TaskEventLogRepository taskEventLogRepository) {
        this.taskExecutionService = taskExecutionService;
        this.taskEventLogRepository = taskEventLogRepository;
    }

    public TaskReplayReport replay(String sessionId, String taskId) {
        TaskExecution execution = requireTask(sessionId, taskId);
        List<Map<String, Object>> events = taskEventLogRepository != null
                ? taskEventLogRepository.findByTaskId(taskId).stream().map(io.github.weiz1103.agentflow.api.domain.model.task.TaskEventLog::getPayload).toList()
                : List.of();
        if (events.isEmpty()) {
            List<Map<String, Object>> replayEvents = taskExecutionService.findReplayEvents(taskId);
            events = replayEvents != null && !replayEvents.isEmpty()
                    ? replayEvents
                    : (execution.getReplayEvents() != null ? execution.getReplayEvents() : List.of());
        }
        return new TaskReplayReport(
                execution.getSessionId(),
                execution.getTaskId(),
                execution.getStatus() != null ? execution.getStatus().name() : "UNKNOWN",
                events,
                execution.getWarnings() != null ? execution.getWarnings() : List.of()
        );
    }

    public TaskScorecard score(String sessionId, String taskId) {
        TaskExecution execution = requireTask(sessionId, taskId);
        int score = 100;
        String verdict = "good";

        TaskExecutionStatus status = execution.getStatus();
        if (status == TaskExecutionStatus.WAITING) {
            score -= 10;
            verdict = "waiting";
        } else if (status == TaskExecutionStatus.FAILED) {
            score -= 60;
            verdict = "failed";
        } else if (status == TaskExecutionStatus.CANCELLED) {
            score -= 40;
            verdict = "cancelled";
        }

        if (execution.isLoopDetected()) {
            score -= 25;
            verdict = "loop_risk";
        }
        if (execution.getRepeatedToolCallCount() > 1) {
            score -= Math.min(20, (int) execution.getRepeatedToolCallCount() * 2);
        }
        if (execution.getToolCallCount() > 12) {
            score -= Math.min(15, (int) (execution.getToolCallCount() - 12));
        }
        if (execution.getErrorMessage() != null && !execution.getErrorMessage().isBlank()) {
            score -= 10;
        }
        score = Math.max(0, score);

        Map<String, Object> dimensions = new LinkedHashMap<>();
        dimensions.put("status", status != null ? status.name() : "UNKNOWN");
        dimensions.put("event_count", execution.getEventCount());
        dimensions.put("tool_call_count", execution.getToolCallCount());
        dimensions.put("step_start_count", execution.getStepStartCount());
        dimensions.put("repeated_tool_call_count", execution.getRepeatedToolCallCount());
        dimensions.put("loop_detected", execution.isLoopDetected());
        dimensions.put("warnings", execution.getWarnings() != null ? execution.getWarnings() : List.of());

        return new TaskScorecard(
                execution.getSessionId(),
                execution.getTaskId(),
                score,
                verdict,
                execution.getTerminalEvent(),
                execution.getErrorMessage(),
                dimensions
        );
    }

    public LoopReport loopReport(int limit) {
        List<TaskExecution> executions = taskExecutionService.findLoopDetected(limit).stream().toList();
        List<LoopReportItem> items = executions.stream().map(execution -> new LoopReportItem(
                execution.getSessionId(),
                execution.getTaskId(),
                execution.getStatus() != null ? execution.getStatus().name() : "UNKNOWN",
                execution.getRepeatedToolCallCount(),
                execution.isLoopDetected(),
                execution.getWarnings() != null ? execution.getWarnings() : List.of(),
                execution.getUpdatedAt()
        )).toList();
        return new LoopReport(items.size(), items);
    }

    private TaskExecution requireTask(String sessionId, String taskId) {
        TaskExecution execution = taskExecutionService.findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException("task execution not found: " + taskId));
        if (!execution.getSessionId().equals(sessionId)) {
            throw new IllegalArgumentException("task does not belong to session: " + sessionId);
        }
        return execution;
    }

    public record TaskReplayReport(
            String sessionId,
            String taskId,
            String status,
            List<Map<String, Object>> events,
            List<String> warnings
    ) {
    }

    public record TaskScorecard(
            String sessionId,
            String taskId,
            int score,
            String verdict,
            String terminalEvent,
            String errorMessage,
            Map<String, Object> dimensions
    ) {
    }

    public record LoopReport(int total, List<LoopReportItem> items) {
    }

    public record LoopReportItem(
            String sessionId,
            String taskId,
            String status,
            long repeatedToolCallCount,
            boolean loopDetected,
            List<String> warnings,
            java.time.LocalDateTime updatedAt
    ) {
    }
}


