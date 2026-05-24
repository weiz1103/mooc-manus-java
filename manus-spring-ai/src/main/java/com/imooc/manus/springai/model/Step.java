package com.imooc.manus.springai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 执行步骤模型，对应Python中的 Step Pydantic BaseModel。
 * 包含步骤id、描述、执行状态、执行结果等信息。
 *
 * @author thezehui@gmail.com
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {

    /** 步骤标识符 */
    @JsonProperty("id")
    private String id;

    /** 步骤描述 */
    @JsonProperty("description")
    private String description;

    /** 执行状态，对应Python的 ExecutionStatus */
    @JsonProperty("status")
    private ExecutionStatus status = ExecutionStatus.PENDING;

    /** 是否成功 */
    @JsonProperty("success")
    private Boolean success;

    /** 步骤执行结果文本 */
    @JsonProperty("result")
    private String result;

    /** 步骤执行结果中的附件路径列表 */
    @JsonProperty("attachments")
    private List<String> attachments;

    /** 错误信息（执行失败时） */
    @JsonProperty("error")
    private String error;

    /**
     * 判断步骤是否已完成（完成或失败均视为完成）。
     * 对应Python的 step.done 属性。
     *
     * @return 是否已完成
     */
    public boolean isDone() {
        return status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED;
    }
}

