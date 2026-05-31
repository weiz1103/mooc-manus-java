package io.github.weiz1103.agentflow.api.domain.exception;

/**
 * 沙箱通信异常，当 agentflow-api 调用 agentflow-sandbox-server 失败时抛出。
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

