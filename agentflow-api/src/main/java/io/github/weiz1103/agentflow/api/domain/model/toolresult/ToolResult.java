package io.github.weiz1103.agentflow.api.domain.model.toolresult;

/**
 * 工具结果Domain模型
 * <p>
 * 包含：是否成功、附加提示信息、以及工具执行结果数据。
 * </p>
 *
 * @param <T> 工具的执行结果数据类。
 * @author zhuang03@qq.com
 * @date 2026-05-29 07:54:06
 */
public record ToolResult<T>(
        /** 是否成功调用 */
        boolean success,
        /** 额外的信息提。*/
        String message,
        /** 工具的执行结。数据 */
        T data
) {
    /**
     * 创建成功的工具结果（无数据）
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> ToolResult<T> ok() {
        return new ToolResult<>(true, "", null);
    }

    /**
     * 创建成功的工具结果（含数据）
     *
     * @param data 工具执行数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> ToolResult<T> ok(T data) {
        return new ToolResult<>(true, "", data);
    }

    /**
     * 创建失败的工具结。
     *
     * @param message 失败原因
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> ToolResult<T> fail(String message) {
        return new ToolResult<>(false, message, null);
    }

    /**
     * 将从沙箱中返回的API数据转换成工具结果。
     *
     * @param code HTTP状态码，小。00视为成功
     * @param msg  附加消息
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 工具结果
     */
    public static <T> ToolResult<T> fromSandbox(int code, String msg, T data) {
        return new ToolResult<>(code < 300, msg, data);
    }
}


