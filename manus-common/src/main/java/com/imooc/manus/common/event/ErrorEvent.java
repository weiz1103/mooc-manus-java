package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 错误事件：Agent 执行出错时发出，对应 Python ErrorEvent。 */
@Data @SuperBuilder @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorEvent extends BaseEvent {

    private String error = "";

    @Override public String getType() { return "error"; }
}
