package com.imooc.manus.api.application.service;

import com.imooc.manus.api.domain.model.task.TaskEventLog;
import com.imooc.manus.api.domain.model.task.TaskExecution;
import com.imooc.manus.api.domain.model.task.TaskExecutionStatus;
import com.imooc.manus.api.domain.repository.TaskEventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEvaluationServiceTest {

    private TaskExecutionService taskExecutionService;
    private TaskEventLogRepository taskEventLogRepository;
    private TaskEvaluationService taskEvaluationService;

    @BeforeEach
    void setUp() {
        taskExecutionService = Mockito.mock(TaskExecutionService.class);
        taskEventLogRepository = Mockito.mock(TaskEventLogRepository.class);
        taskEvaluationService = new TaskEvaluationService(taskExecutionService);
    }

    @Test
    void shouldReplayStoredEvents() {
        TaskExecution execution = sampleExecution();
        execution.setReplayEventsSafe(List.of(Map.of("type", "message", "message", "hello")));
        Mockito.when(taskExecutionService.findByTaskId("task-1")).thenReturn(Optional.of(execution));

        TaskEvaluationService.TaskReplayReport report = taskEvaluationService.replay("session-1", "task-1");

        assertThat(report.taskId()).isEqualTo("task-1");
        assertThat(report.events()).hasSize(1);
        assertThat(report.events().getFirst()).containsEntry("type", "message");
    }

    @Test
    void shouldPenalizeLoopRiskWhenScoring() {
        TaskExecution execution = sampleExecution();
        execution.setLoopDetected(true);
        execution.setRepeatedToolCallCount(5);
        execution.setToolCallCount(15);
        Mockito.when(taskExecutionService.findByTaskId("task-1")).thenReturn(Optional.of(execution));

        TaskEvaluationService.TaskScorecard card = taskEvaluationService.score("session-1", "task-1");

        assertThat(card.score()).isLessThan(100);
        assertThat(card.verdict()).isEqualTo("loop_risk");
        assertThat(card.dimensions()).containsEntry("loop_detected", true);
    }

    @Test
    void shouldPreferTaskEventLogWhenAvailable() {
        TaskExecution execution = sampleExecution();
        execution.setReplayEventsSafe(List.of(Map.of("type", "message", "message", "legacy")));
        Mockito.when(taskExecutionService.findByTaskId("task-1")).thenReturn(Optional.of(execution));
        Mockito.when(taskEventLogRepository.findByTaskId("task-1")).thenReturn(List.of(
                TaskEventLog.of("session-1", "task-1", "1-0", "message", Map.of("type", "message", "message", "from_log"))
        ));

        TaskEvaluationService service = new TaskEvaluationService(taskExecutionService, taskEventLogRepository);
        TaskEvaluationService.TaskReplayReport report = service.replay("session-1", "task-1");

        assertThat(report.events()).hasSize(1);
        assertThat(report.events().getFirst()).containsEntry("message", "from_log");
    }

    @Test
    void shouldBuildLoopReportFromDetectedExecutions() {
        TaskExecution execution = sampleExecution();
        execution.setLoopDetected(true);
        execution.setRepeatedToolCallCount(4);
        execution.setWarningsSafe(List.of("detected_repeated_tool_call:tool|{}"));
        Mockito.when(taskExecutionService.findLoopDetected(10)).thenReturn(List.of(execution));

        TaskEvaluationService.LoopReport report = taskEvaluationService.loopReport(10);

        assertThat(report.total()).isEqualTo(1);
        assertThat(report.items()).hasSize(1);
        assertThat(report.items().getFirst().loopDetected()).isTrue();
    }

    private TaskExecution sampleExecution() {
        TaskExecution execution = TaskExecution.submitted("session-1", "task-1", "task_output_task-1");
        execution.setStatus(TaskExecutionStatus.COMPLETED);
        execution.setUpdatedAt(LocalDateTime.now());
        return execution;
    }
}


