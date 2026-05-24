package com.imooc.manus.api.domain.model.event;

/**
 * 步骤事件状态枚举。
 * 对应Python中的 StepEventStatus。
 *
 * @author thezehui@gmail.com
 */
public enum StepEventStatus {
    /** 已开始 */
    STARTED("started"),
    /** 已完成 */
    COMPLETED("completed"),
    /** 失败 */
    FAILED("failed");

    private final String value;

    StepEventStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

