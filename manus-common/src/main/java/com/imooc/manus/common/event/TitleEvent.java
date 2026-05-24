package com.imooc.manus.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 标题事件：当 AI 生成会话标题时发出，用于更新侧边栏显示，对应 Python TitleEvent。 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TitleEvent extends BaseEvent {

    private String title = "";

    @Override
    public String getType() {
        return "title";
    }
}
