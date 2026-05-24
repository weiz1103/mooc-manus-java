package com.imooc.manus.api.domain.model.event;

/**
 * 等待事件，等待用户输入确认。
 * 对应Python中的 WaitEvent。
 *
 * @author thezehui@gmail.com
 */
public class WaitEvent extends BaseEvent {
    public WaitEvent() {
        super("wait");
    }
}

