package com.imooc.manus.api.domain.model.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 规划Domain模型，用于存储用户传递消息拆分出来的子任务/子步骤。
 * <p>
 * 使用可变POJO，因为规划中的steps在执行过程中会被更新。
 * </p>
 *

 */
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 21:50:27
 */
public class Plan {

    /** 计划id */
    @JsonProperty("id")
    private String id;

    /** 任务标题 */
    @JsonProperty("title")
    private String title;

    /** 任务目标 */
    @JsonProperty("goal")
    private String goal;

    /** 工作语言 */
    @JsonProperty("language")
    private String language;

    /** 步骤列表/子任务列表 */
    @JsonProperty("steps")
    private List<Step> steps;

    /** AI传递的消息 */
    @JsonProperty("message")
    private String message;

    /** 规划的状态 */
    @JsonProperty("status")
    private ExecutionStatus status;

    /** 错误信息 */
    @JsonProperty("error")
    private String error;

    /** 默认构造 */
    public Plan() {
        this.id = UUID.randomUUID().toString();
        this.title = "";
        this.goal = "";
        this.language = "";
        this.steps = new ArrayList<>();
        this.message = "";
        this.status = ExecutionStatus.PENDING;
    }

    /**
     * 只读属性：判断计划是否结束
     *
     * @return 是否已结束
     */
    @JsonIgnore
    public boolean isDone() {
        return status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED;
    }

    /**
     * 获取需要执行的下一个步骤（第一个未完成的步骤）。
     *
     * @return 下一个需要执行的步骤，如果没有则返回empty
     */
    @JsonIgnore
    public Optional<Step> getNextStep() {
        return steps.stream().filter(step -> !step.isDone()).findFirst();
    }

    // ======================== Getters & Setters ========================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}

