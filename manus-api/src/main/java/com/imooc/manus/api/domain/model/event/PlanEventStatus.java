package com.imooc.manus.api.domain.model.event;

/**
 * 规划事件状态枚举。
 * 对应Python中的 PlanEventStatus。
 *
 * @author thezehui@gmail.com
 */
public enum PlanEventStatus {
    /** 已创建 */
    CREATED("created"),
    /** 已更新 */
    UPDATED("updated"),
    /** 已完成 */
    COMPLETED("completed");

    private final String value;

    PlanEventStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

