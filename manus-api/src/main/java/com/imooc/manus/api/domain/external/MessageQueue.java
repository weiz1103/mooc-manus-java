package com.imooc.manus.api.domain.external;

/**
 * 消息队列协议。
 * @author zhuang03@qq.com
 * @date 2026-05-27 05:56:27
 */
public interface MessageQueue {

    /**
     * 往消息队列中添加一条消息
     *
     * @param message 要添加的消息（JSON字符串）
     * @return 消息id
     */
    String put(String message);

    /**
     * 根据传递的开始id+阻塞时间，获取1条数据
     *
     * @param startId 开始id（可为null，从最新的开始）
     * @param blockMs 阻塞时间（毫秒），0表示非阻塞
     * @return 消息id和消息内容的数组（[id, content]），如果没有消息则返回null
     */
    String[] get(String startId, Integer blockMs);

    /**
     * 获取并移除消息队列中的第一条消息
     *
     * @return 消息id和消息内容的数组（[id, content]），如果没有消息则返回null
     */
    String[] pop();

    /**
     * 清空消息队列中的所有消息
     */
    void clear();

    /**
     * 判断消息队列是否为空
     *
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * 获取消息队列的长度
     *
     * @return 队列长度
     */
    long size();

    /**
     * 根据传递的消息id删除队列中指定的消息
     *
     * @param messageId 消息id
     * @return 是否删除成功
     */
    boolean deleteMessage(String messageId);
}

