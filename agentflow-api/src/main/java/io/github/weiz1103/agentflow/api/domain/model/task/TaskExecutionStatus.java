package io.github.weiz1103.agentflow.api.domain.model.task;

/**
 * 任务执行内部状态。
 *
 * <p>该状态面向运行时与恢复逻辑，不直接暴露给前端；前端仍通过 SessionStatus
 * 看到 pending / running / waiting / completed 这组兼容语义。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-26 05:10:10
 */
public enum TaskExecutionStatus {
    SUBMITTED,
    DISPATCHED,
    RUNNING,
    WAITING,
    COMPLETED,
    FAILED,
    CANCEL_REQUESTED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isActive() {
        return !isTerminal();
    }
}


