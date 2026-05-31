package com.imooc.manus.api.infrastructure.event;

import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.domain.repository.TaskEventLogRepository;
import com.imooc.manus.api.infrastructure.config.RedisTaskFactory;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.common.event.MessageEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-26 19:03:59
 */
class EventPersisterTest {

    @Test
    void shouldAppendTaskEventLogWhenSessionHasTaskId() {
        SessionRepository sessionRepository = Mockito.mock(SessionRepository.class);
        RedisTaskFactory taskFactory = Mockito.mock(RedisTaskFactory.class);
        TaskEventLogRepository taskEventLogRepository = Mockito.mock(TaskEventLogRepository.class);
        EventPersister persister = new EventPersister(sessionRepository, taskFactory, taskEventLogRepository);

        Session session = new Session();
        session.setId("session-1");
        session.setTaskId("task-1");

        MessageEvent event = MessageEvent.builder()
                .role("user")
                .message("hello")
                .build();
        event.setId("1-0");

        persister.save(session, event);

        ArgumentCaptor<BaseEvent> eventCaptor = ArgumentCaptor.forClass(BaseEvent.class);
        Mockito.verify(sessionRepository).addEvent(Mockito.eq("session-1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(MessageEvent.class);
        assertThat(((MessageEvent) eventCaptor.getValue()).getMessage()).isEqualTo("hello");
        Mockito.verify(taskEventLogRepository).appendIfAbsent(Mockito.argThat(log ->
                "session-1".equals(log.getSessionId())
                        && "task-1".equals(log.getTaskId())
                        && "1-0".equals(log.getEventId())
                        && "message".equals(log.getEventType())
                        && "hello".equals(log.getPayload().get("message"))
        ));
    }

    @Test
    void shouldSkipTaskEventLogWhenSessionHasNoTask() {
        SessionRepository sessionRepository = Mockito.mock(SessionRepository.class);
        RedisTaskFactory taskFactory = Mockito.mock(RedisTaskFactory.class);
        TaskEventLogRepository taskEventLogRepository = Mockito.mock(TaskEventLogRepository.class);
        EventPersister persister = new EventPersister(sessionRepository, taskFactory, taskEventLogRepository);

        Session session = new Session();
        session.setId("session-1");

        MessageEvent event = MessageEvent.builder().message("hello").build();
        event.setId("1-0");

        persister.save(session, event);

        Mockito.verify(taskEventLogRepository, Mockito.never()).appendIfAbsent(Mockito.any());
    }
}

