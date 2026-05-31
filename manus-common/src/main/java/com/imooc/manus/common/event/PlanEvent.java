package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 包含完整的计划结构和状态，前端用此渲染任务进度面板。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 18:46:21
 */
public class PlanEvent extends BaseEvent {

    private PlanData plan;
    private PlanEventStatus status = PlanEventStatus.CREATED;

    @Override
    public String getType() {
        return "plan";
    }

    /** 计划数据（轻量版，用于 SSE 传输） */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    public static class PlanData {
        private String id;
        private String title = "";
        private String goal = "";
        private String language = "";
        private String message = "";
        private List<StepData> steps = new ArrayList<>();
        private String status = "pending";
    }

    /** 步骤数据 */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    public static class StepData {
        private String id;
        private String description = "";
        private String status = "pending";
        private String result;
        private Boolean success;
    }

    public enum PlanEventStatus {
        CREATED("created"), UPDATED("updated"), COMPLETED("completed");

        private final String value;

        PlanEventStatus(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }
    }
}
