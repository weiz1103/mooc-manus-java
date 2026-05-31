package com.imooc.manus.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结构: { code: 200, msg: "success", data: T }
 *
 * @param <T> 响应数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-27 17:19:11
 */
public class ApiResponse<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().code(200).msg("success").data(data).build();
    }

    public static <T> ApiResponse<T> success(T data, String msg) {
        return ApiResponse.<T>builder().code(200).msg(msg).data(data).build();
    }

    public static <T> ApiResponse<T> success(String msg) {
        return ApiResponse.<T>builder().code(200).msg(msg).build();
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        return ApiResponse.<T>builder().code(code).msg(msg).build();
    }
}
