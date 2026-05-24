package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 标题事件类型。
 * 对应Python中的 TitleEvent。
 *
 * @author thezehui@gmail.com
 */
public class TitleEvent extends BaseEvent {

    /** 标题 */
    @JsonProperty("title")
    private String title;

    public TitleEvent() {
        super("title");
        this.title = "";
    }

    public TitleEvent(String title) {
        super("title");
        this.title = title;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

