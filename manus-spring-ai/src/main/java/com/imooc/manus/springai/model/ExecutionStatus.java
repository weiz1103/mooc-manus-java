package com.imooc.manus.springai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 用于描述 Plan 和 Step 的执行状态。
 * @author zhuang03@qq.com
 * @date 2026-05-28 19:51:24
 */
public enum ExecutionStatus {
    /** 待执行 */
    PENDING,
    /** 执行中 */
    RUNNING,
    /** 已完成 */
    COMPLETED,
    /** 执行失败 */
    FAILED;

    @JsonValue
    public String getValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ExecutionStatus fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (ExecutionStatus status : values()) {
            if (status.name().equalsIgnoreCase(raw)) {
                return status;
            }
        }
        throw new IllegalArgumentException(
                "Unknown ExecutionStatus: " + raw + ", accepted values: " + Arrays.toString(values()));
    }
}

