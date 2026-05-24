package com.imooc.manus.api.agent;

/**
 * 协作式取消异常。
 */
public class TaskCancelledException extends RuntimeException {
    public TaskCancelledException(String message) {
        super(message);
    }
}

