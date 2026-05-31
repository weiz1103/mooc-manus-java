package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 前端用此高亮显示正在执行的步骤。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 11:41:06
 */
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
