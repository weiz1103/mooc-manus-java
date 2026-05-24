package com.imooc.manus.api.domain.exception;

/**
 * 沙箱通信异常，当 manus-api 调用 manus-sandbox-server 失败时抛出。
 */
public class SandboxCommunicationException extends RuntimeException {
    public SandboxCommunicationException(String message) {
        super(message);
    }
    public SandboxCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
