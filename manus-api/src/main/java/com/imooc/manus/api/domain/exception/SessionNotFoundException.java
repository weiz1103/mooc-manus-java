package com.imooc.manus.api.domain.exception;

/**
 * 会话不存在异常，对应 Python NotFoundError。
 * 由 GlobalExceptionHandler 捕获并转换为 404 响应。
 */
public class SessionNotFoundException extends RuntimeException {

    private final String sessionId;

    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
