package com.imooc.manus.api.infrastructure.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.infrastructure.config.AppProperties;
import com.imooc.manus.api.application.service.TaskExecutionService;
import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.common.event.StepEvent;
import com.imooc.manus.common.event.ToolEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础内存版执行观测实现。
 *
 * <p>当前先提供运行期可查询的指标快照与简单循环识别能力，后续可以平滑替换为
 * Metrics/Tracing/OLAP 存储后端。</p>
 */
@Component
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-31 07:47:13
 */
public class InMemoryExecutionObservationSink implements ExecutionObservationSink {

    private static final Logger log = LoggerFactory.getLogger(InMemoryExecutionObservationSink.class);
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private final Map<String, ExecutionSnapshot> byTaskId = new ConcurrentHashMap<>();
    private final Map<String, String> latestTaskBySession = new ConcurrentHashMap<>();
    private final int repeatedToolThreshold;
    private final TaskExecutionService taskExecutionService;

    public InMemoryExecutionObservationSink(AppProperties appProperties,
                                            TaskExecutionService taskExecutionService) {
        this.repeatedToolThreshold = Math.max(2, appProperties.getAgent().getRepeatedToolCallThreshold());
        this.taskExecutionService = taskExecutionService;
    }

    @Override
    public void onTaskSubmitted(String sessionId, String taskId) {
        ExecutionSnapshot snapshot = ExecutionSnapshot.submitted(sessionId, taskId);
        byTaskId.put(taskId, snapshot);
        latestTaskBySession.put(sessionId, taskId);
        taskExecutionService.syncObservation(taskId, snapshot, null);
    }

    @Override
    public void onTaskStarted(String sessionId, String taskId) {
        ExecutionSnapshot snapshot = mutate(taskId, current -> current.toBuilder()
                .status("running")
                .startedAt(current.startedAt() != null ? current.startedAt() : LocalDateTime.now())
                .build());
        taskExecutionService.syncObservation(taskId, snapshot, null);
    }

    @Override
    public void onEvent(String sessionId, String taskId, BaseEvent event) {
        ExecutionSnapshot snapshot = mutate(taskId, current -> {
            ExecutionSnapshot.ExecutionSnapshotBuilder builder = current.toBuilder()
                    .eventCount(current.eventCount() + 1)
                    .terminalEvent(event.getType());

            if (event instanceof StepEvent stepEvent && stepEvent.getStatus() == StepEvent.StepEventStatus.STARTED) {
                builder.stepStartCount(current.stepStartCount() + 1);
            }

            if (event instanceof ToolEvent toolEvent && toolEvent.getStatus() == ToolEvent.ToolEventStatus.CALLING) {
                String fingerprint = fingerprint(toolEvent.getFunctionName(), toolEvent.getFunctionArgs());
                long repeated = current.repeatedToolCallCount();
                boolean loopDetected = current.loopDetected();
                ArrayList<String> warnings = new ArrayList<>(current.warnings());
                if (fingerprint.equals(current.lastToolFingerprint())) {
                    repeated++;
                    if (!loopDetected && repeated >= repeatedToolThreshold) {
                        loopDetected = true;
                        warnings.add("detected_repeated_tool_call:" + fingerprint);
                    }
                } else {
                    repeated = 1;
                }
                builder.toolCallCount(current.toolCallCount() + 1)
                        .lastToolFingerprint(fingerprint)
                        .repeatedToolCallCount(repeated)
                        .loopDetected(loopDetected)
                        .warnings(warnings);
            }

            switch (event.getType()) {
                case "wait" -> builder.status("waiting");
                case "done" -> builder.status("completed").finishedAt(LocalDateTime.now());
                case "error" -> builder.status("failed").finishedAt(LocalDateTime.now());
                default -> {
                }
            }
            return builder.build();
        });
        taskExecutionService.syncObservation(taskId, snapshot, event);
    }

    @Override
    public void onTaskFinished(String sessionId, String taskId) {
        ExecutionSnapshot snapshot = mutate(taskId, current -> {
            if ("waiting".equals(current.status())) {
                return current;
            }
            if ("failed".equals(current.status()) || "completed".equals(current.status()) || "cancelled".equals(current.status())) {
                return current.finishedAt() != null
                        ? current
                        : current.toBuilder().finishedAt(LocalDateTime.now()).build();
            }
            return current.toBuilder()
                    .status("completed")
                    .finishedAt(LocalDateTime.now())
                    .build();
        });
        taskExecutionService.syncObservation(taskId, snapshot, null);
    }

    @Override
    public void onTaskFailed(String sessionId, String taskId, String errorMessage) {
        ExecutionSnapshot snapshot = mutate(taskId, current -> current.toBuilder()
                .status("failed")
                .errorMessage(errorMessage)
                .finishedAt(LocalDateTime.now())
                .build());
        taskExecutionService.syncObservation(taskId, snapshot, null);
        log.warn("任务执行失败: sessionId={}, taskId={}, error={}", sessionId, taskId, errorMessage);
    }

    @Override
    public void onTaskCancelled(String sessionId, String taskId, String reason) {
        ExecutionSnapshot snapshot = mutate(taskId, current -> current.toBuilder()
                .status("cancelled")
                .errorMessage(reason)
                .finishedAt(LocalDateTime.now())
                .terminalEvent(current.terminalEvent() != null ? current.terminalEvent() : "done")
                .build());
        taskExecutionService.syncObservation(taskId, snapshot, null);
    }

    @Override
    public Optional<ExecutionSnapshot> getByTaskId(String taskId) {
        return Optional.ofNullable(byTaskId.get(taskId));
    }

    @Override
    public Optional<ExecutionSnapshot> getLatestBySessionId(String sessionId) {
        String taskId = latestTaskBySession.get(sessionId);
        return taskId == null ? Optional.empty() : getByTaskId(taskId);
    }

    private ExecutionSnapshot mutate(String taskId, java.util.function.Function<ExecutionSnapshot, ExecutionSnapshot> updater) {
        return byTaskId.compute(taskId,
                (k, current) -> updater.apply(current != null ? current : ExecutionSnapshot.submitted("unknown", taskId)));
    }

    private String fingerprint(String functionName, Map<String, Object> args) {
        try {
            return functionName + "|" + JSON.writeValueAsString(args == null ? Map.of() : args);
        } catch (Exception e) {
            return functionName + "|{}";
        }
    }
}

