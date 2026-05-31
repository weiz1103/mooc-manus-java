package com.imooc.manus.api.application.agent;

/**
 * 协作式取消异常。
 * @author zhuang03@qq.com
 * @date 2026-05-28 15:20:09
 */
public class TaskCancelledException extends RuntimeException {
    public TaskCancelledException(String message) {
        super(message);
    }
}

