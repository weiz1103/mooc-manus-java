package io.github.weiz1103.agentflow.api.domain.repository;

import io.github.weiz1103.agentflow.common.event.BaseEvent;
import io.github.weiz1103.agentflow.api.domain.model.file.FileMeta;
import io.github.weiz1103.agentflow.api.domain.model.memory.Memory;
import io.github.weiz1103.agentflow.api.domain.model.session.Session;
import io.github.weiz1103.agentflow.api.domain.model.session.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会话仓库协议定义。
 * @author zhuang03@qq.com
 * @date 2026-05-30 07:18:30
 */
public interface SessionRepository {

    /**
     * 存储或更新传递进来的会话
     *
     * @param session 会话领域模型
     */
    void save(Session session);

    /**
     * 获取所有会话列表信。
     *
     * @return 会话列表
     */
    List<Session> getAll();

    /**
     * 根据传递的会话id查询会话
     *
     * @param sessionId 会话id
     * @return 会话（Optional。
     */
    Optional<Session> getById(String sessionId);

    /**
     * 根据传递的会话id删除会话
     *
     * @param sessionId 会话id
     */
    void deleteById(String sessionId);

    /**
     * 根据传递的会话id+标题更新会话信息
     *
     * @param sessionId 会话id
     * @param title     新标。
     */
    void updateTitle(String sessionId, String title);

    /**
     * 根据传递的信息更新最新消。
     *
     * @param sessionId 会话id
     * @param message   最新消息内。
     * @param timestamp 消息时间
     */
    void updateLatestMessage(String sessionId, String message, LocalDateTime timestamp);

    /**
     * 根据传递的信息更新未读消息。
     *
     * @param sessionId 会话id
     * @param count     未读消息。
     */
    void updateUnreadMessageCount(String sessionId, int count);

    /**
     * 根据传递的会话id新增未读消息。
     *
     * @param sessionId 会话id
     */
    void incrementUnreadMessageCount(String sessionId);

    /**
     * 根据传递的会话id减少未读消息。
     *
     * @param sessionId 会话id
     */
    void decrementUnreadMessageCount(String sessionId);

    /**
     * 根据传递的会话id更新会话状。
     *
     * @param sessionId 会话id
     * @param status    新状。
     */
    void updateStatus(String sessionId, SessionStatus status);

    /**
     * 往会话中新增事。
     *
     * @param sessionId 会话id
     * @param event     要添加的事件
     */
    void addEvent(String sessionId, BaseEvent event);

    /**
     * 往会话中新增原始事件数据（Map 格式，跳过领域模型类型转换）。
     * 。EventPersister 使用，避。common.event 。domain.model.event 之间字段类型不兼容。
     *
     * @param sessionId 会话id
     * @param eventData 事件的原始键值对
     */
    

    /**
     * 往会话中新增文。
     *
     * @param sessionId 会话id
     * @param file      要添加的文件
     */
    void addFile(String sessionId, FileMeta file);

    /**
     * 根据传递的会话id+文件id移除文件
     *
     * @param sessionId 会话id
     * @param fileId    文件id（对应filepath。
     */
    void removeFile(String sessionId, String fileId);

    /**
     * 查询会话中的文件信息
     *
     * @param sessionId 会话id
     * @param filepath  文件路径
     * @return 文件信息（Optional。
     */
    Optional<FileMeta> getFileByPath(String sessionId, String filepath);

    /**
     * 更新or创建会话中指定Agent的记。
     *
     * @param sessionId 会话id
     * @param agentName Agent名字
     * @param memory    记忆
     */
    void saveMemory(String sessionId, String agentName, Memory memory);

    /**
     * 根据传递的会话id+Agent名字获取记忆
     *
     * @param sessionId 会话id
     * @param agentName Agent名字
     * @return 记忆
     */
    Memory getMemory(String sessionId, String agentName);
}


