package com.imooc.manus.api.event;

import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.domain.repository.TaskEventLogRepository;
import com.imooc.manus.api.infrastructure.config.RedisTaskFactory;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.common.event.MessageEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EventPersisterTest {

    @Test
    @SuppressWarnings("unchecked")
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

        ArgumentCaptor<Map<String, Object>> eventDataCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(sessionRepository).addEventData(Mockito.eq("session-1"), eventDataCaptor.capture());
        assertThat(eventDataCaptor.getValue()).containsEntry("type", "message");
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


