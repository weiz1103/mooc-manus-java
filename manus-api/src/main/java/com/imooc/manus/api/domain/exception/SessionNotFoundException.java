package com.imooc.manus.api.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 会话不存在异常。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 03:20:24
 */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String sessionId) {
        super("会话不存在: " + sessionId);
    }
}
