package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 等待事件：Agent 需要等待用户输入时发出，对应 Python WaitEvent。*/
@Data @SuperBuilder @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WaitEvent extends BaseEvent {
    @Override public String getType() { return "wait"; }
}
