package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data @SuperBuilder @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 17:26:25
 */
public class WaitEvent extends BaseEvent {
    @Override public String getType() { return "wait"; }
}
