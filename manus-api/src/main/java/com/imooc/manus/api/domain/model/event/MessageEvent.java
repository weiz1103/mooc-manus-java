package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.imooc.manus.api.domain.model.file.FileMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息事件，包含人类消息和AI消息。
 * 对应Python中的 MessageEvent。
 *
 * @author thezehui@gmail.com
 */
public class MessageEvent extends BaseEvent {

    /** 消息角色（user/assistant） */
    @JsonProperty("role")
    private String role;

    /** 消息本身 */
    @JsonProperty("message")
    private String message;

    /** 附件列表信息 */
    @JsonProperty("attachments")
    private List<FileMeta> attachments;

    public MessageEvent() {
        super("message");
        this.role = "assistant";
        this.message = "";
        this.attachments = new ArrayList<>();
    }

    public MessageEvent(String role, String message) {
        super("message");
        this.role = role;
        this.message = message;
        this.attachments = new ArrayList<>();
    }

    public MessageEvent(String role, String message, List<FileMeta> attachments) {
        super("message");
        this.role = role;
        this.message = message;
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<FileMeta> getAttachments() { return attachments; }
    public void setAttachments(List<FileMeta> attachments) { this.attachments = attachments; }
}

