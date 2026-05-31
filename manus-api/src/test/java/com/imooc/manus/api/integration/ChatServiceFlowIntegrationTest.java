package com.imooc.manus.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.application.agent.AgentRunner;
import com.imooc.manus.api.application.service.AgentTaskService;
import com.imooc.manus.api.application.service.ChatService;
import com.imooc.manus.api.application.service.ExecutionAdmissionService;
import com.imooc.manus.api.application.service.TaskExecutionService;
import com.imooc.manus.api.domain.external.MessageQueue;
import com.imooc.manus.api.domain.external.Task;
import com.imooc.manus.api.domain.external.TaskDispatchQueue;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.repository.SessionRepository;
import com.imooc.manus.api.infrastructure.config.AppProperties;
import com.imooc.manus.api.infrastructure.event.AgentEventBus;
import com.imooc.manus.api.infrastructure.observability.ExecutionObservationSink;
import com.imooc.manus.api.interfaces.sse.SsePublisher;
import com.imooc.manus.common.event.BaseEvent;
import com.imooc.manus.common.event.DoneEvent;
import com.imooc.manus.common.event.MessageEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ChatServiceFlowIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldSubmitAsyncTaskWithoutRedisRuntime() {
        SessionRepository sessionRepository = Mockito.mock(SessionRepository.class);
        AgentTaskService agentTaskService = Mockito.mock(AgentTaskService.class);
        AgentRunner agentRunner = Mockito.mock(AgentRunner.class);
        AgentEventBus eventBus = Mockito.mock(AgentEventBus.class);
        TaskDispatchQueue taskDispatchQueue = Mockito.mock(TaskDispatchQueue.class);
        TaskExecutionService taskExecutionService = Mockito.mock(TaskExecutionService.class);
        ExecutionAdmissionService executionAdmissionService = Mockito.mock(ExecutionAdmissionService.class);
        ExecutionObservationSink observationSink = Mockito.mock(ExecutionObservationSink.class);
        SsePublisher publisher = Mockito.mock(SsePublisher.class);

        AppProperties properties = new AppProperties();
        properties.getAgent().setAsyncExecutionEnabled(true);

        ChatService chatService = new ChatService(
                sessionRepository,
                agentTaskService,
                agentRunner,
                eventBus,
                taskDispatchQueue,
                taskExecutionService,
                executionAdmissionService,
                observationSink,
                properties
        );

        Session session = new Session();
        session.setId("session-1");
        session.setTaskId("task-1");
        Task task = new FakeTask("task-1");
        LocalDateTime timestamp = LocalDateTime.of(2026, 5, 31, 12, 0, 0);

        Mockito.when(sessionRepository.getById("session-1")).thenReturn(Optional.of(session));
        Mockito.when(agentTaskService.ensureTask(session)).thenReturn(task);
        Mockito.when(executionAdmissionService.rejectReason("session-1")).thenReturn(Optional.empty());
        Mockito.when(taskDispatchQueue.submit(any())).thenReturn("dispatch-1");
        Mockito.when(taskDispatchQueue.getConsumerGroup()).thenReturn("manus-agent-workers");
        Mockito.when(publisher.isClientGone()).thenReturn(true);

        chatService.sendMessage("session-1", "hello", List.of("/tmp/a.txt"), timestamp, publisher);

        Mockito.verify(sessionRepository).updateLatestMessage("session-1", "hello", timestamp);
        Mockito.verify(taskExecutionService).registerSubmitted("session-1", "task-1", "task_output_task-1");
        Mockito.verify(taskExecutionService).markDispatched("task-1", "dispatch-1", "manus-agent-workers");
        Mockito.verify(observationSink).onTaskSubmitted("session-1", "task-1");

        ArgumentCaptor<BaseEvent> eventCaptor = ArgumentCaptor.forClass(BaseEvent.class);
        Mockito.verify(eventBus).dispatch(eq(session), eventCaptor.capture(), eq(publisher));
        assertThat(eventCaptor.getValue()).isInstanceOf(MessageEvent.class);
        MessageEvent event = (MessageEvent) eventCaptor.getValue();
        assertThat(event.getRole()).isEqualTo("user");
        assertThat(event.getMessage()).isEqualTo("hello");
        assertThat(event.getAttachments()).containsExactly("/tmp/a.txt");
    }

    @Test
    void shouldResumeStreamFromFakeTaskOutputWithoutRedisRuntime() throws Exception {
        SessionRepository sessionRepository = Mockito.mock(SessionRepository.class);
        AgentTaskService agentTaskService = Mockito.mock(AgentTaskService.class);
        AgentRunner agentRunner = Mockito.mock(AgentRunner.class);
        AgentEventBus eventBus = Mockito.mock(AgentEventBus.class);
        TaskDispatchQueue taskDispatchQueue = Mockito.mock(TaskDispatchQueue.class);
        TaskExecutionService taskExecutionService = Mockito.mock(TaskExecutionService.class);
        ExecutionAdmissionService executionAdmissionService = Mockito.mock(ExecutionAdmissionService.class);
        ExecutionObservationSink observationSink = Mockito.mock(ExecutionObservationSink.class);
        SsePublisher publisher = Mockito.mock(SsePublisher.class);

        AppProperties properties = new AppProperties();
        properties.getAgent().setAsyncExecutionEnabled(true);

        ChatService chatService = new ChatService(
                sessionRepository,
                agentTaskService,
                agentRunner,
                eventBus,
                taskDispatchQueue,
                taskExecutionService,
                executionAdmissionService,
                observationSink,
                properties
        );

        Session session = new Session();
        session.setId("session-1");
        session.setTaskId("task-1");
        session.start();

        FakeTask task = new FakeTask("task-1");
        task.output.put(JSON.writeValueAsString(MessageEvent.builder()
                .role("assistant")
                .message("working")
                .attachments(List.of())
                .build()));
        task.output.put(JSON.writeValueAsString(DoneEvent.builder().build()));

        Mockito.when(sessionRepository.getById("session-1")).thenReturn(Optional.of(session));
        Mockito.when(agentTaskService.findTask(session)).thenReturn(Optional.of(task));
        Mockito.when(publisher.isClientGone()).thenReturn(false);
        Mockito.when(publisher.publish(any())).thenReturn(true);

        chatService.resumeStream("session-1", "0", publisher);

        ArgumentCaptor<BaseEvent> eventCaptor = ArgumentCaptor.forClass(BaseEvent.class);
        Mockito.verify(publisher, Mockito.times(2)).publish(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues())
                .extracting(BaseEvent::getType)
                .containsExactly("message", "done");
    }

    private static final class FakeTask implements Task {
        private final String id;
        private final InMemoryMessageQueue input = new InMemoryMessageQueue();
        private final InMemoryMessageQueue output = new InMemoryMessageQueue();

        private FakeTask(String id) {
            this.id = id;
        }

        @Override
        public MessageQueue getInputStream() {
            return input;
        }

        @Override
        public MessageQueue getOutputStream() {
            return output;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    private static final class InMemoryMessageQueue implements MessageQueue {
        private final AtomicLong sequence = new AtomicLong(0);
        private final List<String[]> records = new ArrayList<>();

        @Override
        public synchronized String put(String message) {
            String id = sequence.incrementAndGet() + "-0";
            records.add(new String[]{id, message});
            return id;
        }

        @Override
        public synchronized String[] get(String startId, Integer blockMs) {
            if (records.isEmpty()) {
                return null;
            }
            if (startId == null || startId.isBlank()) {
                return records.get(0);
            }
            for (String[] record : records) {
                if (record[0].compareTo(startId) > 0) {
                    return record;
                }
            }
            return null;
        }

        @Override
        public synchronized String[] pop() {
            return records.isEmpty() ? null : records.remove(0);
        }

        @Override
        public synchronized void clear() {
            records.clear();
        }

        @Override
        public synchronized boolean isEmpty() {
            return records.isEmpty();
        }

        @Override
        public synchronized long size() {
            return records.size();
        }

        @Override
        public synchronized boolean deleteMessage(String messageId) {
            return records.removeIf(record -> record[0].equals(messageId));
        }
    }
}

