package com.imooc.manus.api.domain.exception;

/**
 * 沙箱通信异常，当 manus-api 调用 manus-sandbox-server 失败时抛出。
 * @author zhuang03@qq.com
 * @date 2026-05-25 17:20:19
 */
public class SandboxCommunicationException extends RuntimeException {
    public SandboxCommunicationException(String message) {
        super(message);
    }
    public SandboxCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
