package io.github.weiz1103.agentflow.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 包含工具名、函数名、参数和结果，前端用此渲染工具调用动画和结果展示。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolEvent extends BaseEvent {

    private String toolCallId;
    private String toolName;
    private String functionName;
    private Map<String, Object> functionArgs;
    private Object toolContent;
    private ToolEventStatus status = ToolEventStatus.CALLING;

    @Override
    public String getType() {
        return "tool";
    }

    public enum ToolEventStatus {
        CALLING("calling"), CALLED("called");

        private final String value;

        ToolEventStatus(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }
    }
}

