package io.github.weiz1103.agentflow.api.domain.external;

/**
 * 定义任务相关的操作接口协议。
 * <p>
 * 任务现在仅作为输。输出流的数据载体。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-27 07:40:07
 */
public interface Task {

    /**
     * 只读属性，返回任务的输入流
     *
     * @return 输入流（Redis Stream。
     */
    MessageQueue getInputStream();

    /**
     * 只读属性，返回任务的输出流
     *
     * @return 输出流（Redis Stream。
     */
    MessageQueue getOutputStream();

    /**
     * 只读属性，返回任务的id
     *
     * @return 任务id
     */
    String getId();
}

