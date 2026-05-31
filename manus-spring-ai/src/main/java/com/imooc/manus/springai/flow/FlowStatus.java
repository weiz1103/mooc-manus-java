package com.imooc.manus.springai.flow;

/**
 * 规划与执行流的状态枚举。
 * @author zhuang03@qq.com
 * @date 2026-05-28 11:47:23
 */
public enum FlowStatus {
    /** 空闲状态，等待新任务 */
    IDLE,
    /** 规划中：PlannerAgent 正在生成/重新生成规划 */
    PLANNING,
    /** 执行中：ReActAgent 正在执行子步骤 */
    EXECUTING,
    /** 更新规划中：PlannerAgent 根据已完成步骤更新规划 */
    UPDATING,
    /** 总结中：ReActAgent 正在汇总所有步骤结果 */
    SUMMARIZING,
    /** 已完成：所有子任务完成，流程结束 */
    COMPLETED
}

