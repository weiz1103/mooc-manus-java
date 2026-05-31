package io.github.weiz1103.agentflow.springai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 包含任务标题、目标、语言、步骤列表、状态等信息。
 *

 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Plan {

    /** 规划id */
    @JsonProperty("id")
    private String id = UUID.randomUUID().toString();

    /** 任务标题 */
    @JsonProperty("title")
    private String title = "";

    /** 任务目标 */
    @JsonProperty("goal")
    private String goal = "";

    /** 工作语言（如"中文"。English"。*/
    @JsonProperty("language")
    private String language = "";

    /** 步骤列表/子任务列。*/
    @JsonProperty("steps")
    private List<Step> steps = new ArrayList<>();

    /** AI传递的消息（规划结果摘要） */
    @JsonProperty("message")
    private String message = "";

    /** 规划的执行状。*/
    @JsonProperty("status")
    private ExecutionStatus status = ExecutionStatus.PENDING;

    /** 错误信息 */
    @JsonProperty("error")
    private String error;

    /**
     * 获取下一个需要执行的子步骤（第一个未完成的步骤）。
     *
     * @return 下一个待执行步骤，如果所有步骤已完成则返。empty
     */
    @JsonIgnore
    public Optional<Step> getNextStep() {
        return steps.stream().filter(step -> !step.isDone()).findFirst();
    }

    /**
     * 判断规划是否已完成（全部步骤完成或失败）。
     *
     * @return 是否已完。
     */
    @JsonIgnore
    public boolean isDone() {
        return status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED;
    }
}


