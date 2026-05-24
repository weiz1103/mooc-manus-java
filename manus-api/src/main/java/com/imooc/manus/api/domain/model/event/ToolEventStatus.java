package com.imooc.manus.api.domain.model.event;

/**
 * 工具事件状态类型枚举。
 * 对应Python中的 ToolEventStatus。
 *
 * @author thezehui@gmail.com
 */
public enum ToolEventStatus {
    /** 调用中 */
    CALLING("calling"),
    /** 调用完毕 */
    CALLED("called");

    private final String value;

    ToolEventStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

