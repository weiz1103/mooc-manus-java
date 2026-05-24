package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 步骤事件：当单个计划步骤状态变化时发出，对应 Python StepEvent。
 * 前端用此高亮显示正在执行的步骤。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StepEvent extends BaseEvent {

    private StepData step;
    private StepEventStatus status = StepEventStatus.STARTED;

    @Override
    public String getType() {
        return "step";
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    public static class StepData {
        private String id;
        private String description;
        private String status;
    }

    public enum StepEventStatus {
        STARTED("started"), COMPLETED("completed"), FAILED("failed");

        private final String value;

        StepEventStatus(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }
    }
}
