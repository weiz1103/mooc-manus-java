package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 错误事件。
 * 对应Python中的 ErrorEvent。
 *
 * @author thezehui@gmail.com
 */
public class ErrorEvent extends BaseEvent {

    /** 错误信息 */
    @JsonProperty("error")
    private String error;

    public ErrorEvent() {
        super("error");
        this.error = "";
    }

    public ErrorEvent(String error) {
        super("error");
        this.error = error;
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}

