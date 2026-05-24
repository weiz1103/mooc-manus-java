package com.imooc.manus.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用统一结果包装器，对应 Python ToolResult。
 * 封装工具执行的成功/失败状态和返回数据。
 *
 * @param <T> 工具返回数据的类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolResult<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ToolResult<T> success(T data) {
        return ToolResult.<T>builder().success(true).message("success").data(data).build();
    }

    public static <T> ToolResult<T> success(T data, String message) {
        return ToolResult.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ToolResult<T> error(String message) {
        return ToolResult.<T>builder().success(false).message(message).build();
    }
}
