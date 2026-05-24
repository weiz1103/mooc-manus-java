package com.imooc.manus.api.domain.model.plan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 规划中每一个子步骤/子任务模型。
 * <p>
 * 对应Python中的 Step Pydantic BaseModel。
 * 使用可变POJO（非record），因为步骤状态在执行过程中会被修改。
 * </p>
 *
 * @author thezehui@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {

    /** 子任务id */
    @JsonProperty("id")
    private String id;

    /** 步骤的描述信息 */
    @JsonProperty("description")
    private String description;

    /** 子任务的执行状态 */
    @JsonProperty("status")
    private ExecutionStatus status;

    /** 结果 */
    @JsonProperty("result")
    private String result;

    /** 错误信息 */
    @JsonProperty("error")
    private String error;

    /** 是否执行成功 */
    @JsonProperty("success")
    private boolean success;

    /** 附件列表信息 */
    @JsonProperty("attachments")
    private List<String> attachments;

    /** 默认构造 */
    public Step() {
        this.id = UUID.randomUUID().toString();
        this.description = "";
        this.status = ExecutionStatus.PENDING;
        this.success = false;
        this.attachments = new ArrayList<>();
    }

    /**
     * 只读属性：步骤是否已结束（已完成或失败）
     *
     * @return 是否已结束
     */
    public boolean isDone() {
        return status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED;
    }

    // ======================== Getters & Setters ========================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }
}

