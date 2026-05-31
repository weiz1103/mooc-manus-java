package io.github.weiz1103.agentflow.api.domain.model.session;

/**
 * 会话状态类型枚举。
 * @author zhuang03@qq.com
 * @date 2026-05-27 10:32:47
 */
public enum SessionStatus {
    /** 等待任务 */
    PENDING("pending"),
    /** 运行。*/
    RUNNING("running"),
    /** 等待人类响应 */
    WAITING("waiting"),
    /** 已完。*/
    COMPLETED("completed");

    private final String value;

    SessionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getDatabaseValue() {
        return name();
    }

    public static SessionStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        for (SessionStatus s : values()) {
            if (s.value.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        return PENDING;
    }
}


