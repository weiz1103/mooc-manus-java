package com.imooc.manus.api.interfaces.rest;

import com.imooc.manus.api.service.TaskEvaluationService;
import com.imooc.manus.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务回放与离线评估接口。
 *
 * <p>属于新增后端能力，不影响现有前端 SSE 契约。</p>
 */
@RestController
public class TaskEvaluationController {

    private final TaskEvaluationService taskEvaluationService;

    public TaskEvaluationController(TaskEvaluationService taskEvaluationService) {
        this.taskEvaluationService = taskEvaluationService;
    }

    @GetMapping({"/api/sessions/{sessionId}/tasks/{taskId}/replay", "/api/v1/sessions/{sessionId}/tasks/{taskId}/replay"})
    public ResponseEntity<ApiResponse<TaskEvaluationService.TaskReplayReport>> replay(
            @PathVariable String sessionId,
            @PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.success(taskEvaluationService.replay(sessionId, taskId), "获取任务回放成功"));
    }

    @GetMapping({"/api/sessions/{sessionId}/tasks/{taskId}/evaluation", "/api/v1/sessions/{sessionId}/tasks/{taskId}/evaluation"})
    public ResponseEntity<ApiResponse<TaskEvaluationService.TaskScorecard>> evaluate(
            @PathVariable String sessionId,
            @PathVariable String taskId) {
        return ResponseEntity.ok(ApiResponse.success(taskEvaluationService.score(sessionId, taskId), "获取任务执行评估成功"));
    }

    @GetMapping({"/api/evaluations/loops", "/api/v1/evaluations/loops"})
    public ResponseEntity<ApiResponse<TaskEvaluationService.LoopReport>> loopReport(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(taskEvaluationService.loopReport(limit), "获取循环报表成功"));
    }
}

