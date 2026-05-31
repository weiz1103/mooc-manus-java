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
 * @date 2026-05-25 16:40:44
 */
public class ErrorEvent extends BaseEvent {

    private String error = "";

    @Override public String getType() { return "error"; }
}
