package com.imooc.manus.api.infrastructure.observability;

import com.imooc.manus.common.event.BaseEvent;

import java.util.Optional;

/**
 * Agent 执行观测旁路。
 *
 * <p>不改变主链路业务语义，只负责采集任务执行指标、循环信号和终止结果，
 * 供后续 metrics / tracing / evaluation 扩展。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-31 07:36:31
 */
public interface ExecutionObservationSink {

    void onTaskSubmitted(String sessionId, String taskId);

    void onTaskStarted(String sessionId, String taskId);

    void onEvent(String sessionId, String taskId, BaseEvent event);

    void onTaskFinished(String sessionId, String taskId);

    void onTaskFailed(String sessionId, String taskId, String errorMessage);

    void onTaskCancelled(String sessionId, String taskId, String reason);

    Optional<ExecutionSnapshot> getByTaskId(String taskId);

    Optional<ExecutionSnapshot> getLatestBySessionId(String sessionId);
}

