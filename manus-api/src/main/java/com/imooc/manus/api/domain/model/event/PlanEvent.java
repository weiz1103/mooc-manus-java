package com.imooc.manus.api.domain.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.imooc.manus.api.domain.model.plan.Plan;

/**
 * 规划事件类型。
 * 对应Python中的 PlanEvent。
 *
 * @author thezehui@gmail.com
 */
public class PlanEvent extends BaseEvent {

    /** 规划 */
    @JsonProperty("plan")
    private Plan plan;

    /** 规划事件状态 */
    @JsonProperty("status")
    private PlanEventStatus status;

    public PlanEvent() {
        super("plan");
        this.status = PlanEventStatus.CREATED;
    }

    public PlanEvent(Plan plan, PlanEventStatus status) {
        super("plan");
        this.plan = plan;
        this.status = status;
    }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public PlanEventStatus getStatus() { return status; }
    public void setStatus(PlanEventStatus status) { this.status = status; }
}

