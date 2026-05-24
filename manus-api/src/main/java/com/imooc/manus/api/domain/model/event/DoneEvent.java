package com.imooc.manus.api.domain.model.event;

/**
 * 结束事件类型。
 * 对应Python中的 DoneEvent。
 *
 * @author thezehui@gmail.com
 */
public class DoneEvent extends BaseEvent {
    public DoneEvent() {
        super("done");
    }
}

