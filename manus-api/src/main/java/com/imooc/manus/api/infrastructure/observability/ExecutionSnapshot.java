package com.imooc.manus.api.infrastructure.observability;

import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 一次 Agent 任务执行的观测快照。
 */
@Builder(toBuilder = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 18:11:13
 */
public record ExecutionSnapshot(
        String sessionId,
        String taskId,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        long eventCount,
        long toolCallCount,
        long stepStartCount,
        long repeatedToolCallCount,
        boolean loopDetected,
        String lastToolFingerprint,
        String terminalEvent,
        String errorMessage,
        List<String> warnings
) {
    public ExecutionSnapshot {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public long durationMs() {
        if (startedAt == null) {
            return 0L;
        }
        LocalDateTime end = finishedAt != null ? finishedAt : LocalDateTime.now();
        return Duration.between(startedAt, end).toMillis();
    }

    public static ExecutionSnapshot submitted(String sessionId, String taskId) {
        return ExecutionSnapshot.builder()
                .sessionId(sessionId)
                .taskId(taskId)
                .status("submitted")
                .submittedAt(LocalDateTime.now())
                .eventCount(0)
                .toolCallCount(0)
                .stepStartCount(0)
                .repeatedToolCallCount(0)
                .loopDetected(false)
                .warnings(new ArrayList<>())
                .build();
    }
}

