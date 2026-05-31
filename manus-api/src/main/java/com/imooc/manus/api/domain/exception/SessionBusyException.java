package com.imooc.manus.api.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 会话正忙异常（AI 执行中，拒绝新消息）。
 */
@ResponseStatus(HttpStatus.CONFLICT)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 05:10:53
 */
public class SessionBusyException extends RuntimeException {

    public SessionBusyException(String sessionId) {
        super("会话正在执行中，请等待当前任务完成后再发送消息: " + sessionId);
    }
}
