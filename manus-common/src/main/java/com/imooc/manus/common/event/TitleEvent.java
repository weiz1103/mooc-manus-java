package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 12:18:40
 */
public class TitleEvent extends BaseEvent {

    private String title = "";

    @Override
    public String getType() {
        return "title";
    }
}
