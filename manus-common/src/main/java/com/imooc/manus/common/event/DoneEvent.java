package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 完成事件：Agent 完成一轮任务后发出，触发前端 UI 的完成状态渲染，对应 Python DoneEvent。 */
@Data @SuperBuilder @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DoneEvent extends BaseEvent {
    @Override public String getType() { return "done"; }
}
