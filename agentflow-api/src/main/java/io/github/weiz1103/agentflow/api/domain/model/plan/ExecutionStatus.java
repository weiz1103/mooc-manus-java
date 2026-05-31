package io.github.weiz1103.agentflow.api.domain.model.plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 规划/任务执行状态枚举。
 * <p>
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-31 17:22:59
 */
public enum ExecutionStatus {
    /** 空闲or等待。*/
    PENDING("pending"),
    /** 执行。*/
    RUNNING("running"),
    /** 执行完成 */
    COMPLETED("completed"),
    /** 失败 */
    FAILED("failed");

    private final String value;

    ExecutionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ExecutionStatus fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (ExecutionStatus status : values()) {
            if (status.value.equalsIgnoreCase(raw) || status.name().equalsIgnoreCase(raw)) {
                return status;
            }
        }
        throw new IllegalArgumentException(
                "Unknown ExecutionStatus: " + raw + ", accepted values: " + Arrays.toString(values()));
    }
}


