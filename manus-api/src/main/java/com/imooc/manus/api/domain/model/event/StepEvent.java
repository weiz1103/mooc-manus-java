package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.imooc.manus.api.domain.model.plan.Step;

/**
 * 子任务/步骤事件。
 * 对应Python中的 StepEvent。
 *
 * @author thezehui@gmail.com
 */
public class StepEvent extends BaseEvent {

    /** 步骤信息 */
    @JsonProperty("step")
    private Step step;

    /** 步骤事件状态 */
    @JsonProperty("status")
    private StepEventStatus status;

    public StepEvent() {
        super("step");
        this.status = StepEventStatus.STARTED;
    }

    public StepEvent(Step step, StepEventStatus status) {
        super("step");
        this.step = step;
        this.status = status;
    }

    public Step getStep() { return step; }
    public void setStep(Step step) { this.step = step; }

    public StepEventStatus getStatus() { return status; }
    public void setStatus(StepEventStatus status) { this.status = status; }
}

