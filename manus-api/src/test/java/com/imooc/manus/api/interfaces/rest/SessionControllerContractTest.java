package com.imooc.manus.api.interfaces.rest;

import com.imooc.manus.api.domain.model.file.FileMeta;
import com.imooc.manus.api.domain.model.session.Session;
import com.imooc.manus.api.domain.model.session.SessionStatus;
import com.imooc.manus.api.observability.ExecutionObservationSink;
import com.imooc.manus.api.observability.ExecutionSnapshot;
import com.imooc.manus.api.service.ChatService;
import com.imooc.manus.api.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
class SessionControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;
    @MockitoBean
    private ChatService chatService;
    @MockitoBean
    private ExecutionObservationSink executionObservationSink;

    private Session testSession;

    @BeforeEach
    void setUp() {
        testSession = new Session();
        testSession.setId("test-session-123");
        testSession.setTitle("Test Session");
        testSession.restore(SessionStatus.PENDING);
        testSession.setLatestMessage("Hello");
        testSession.setLatestMessageAt(LocalDateTime.now());
        testSession.setUnreadMessageCount(1);
        testSession.setEvents(List.of());
        testSession.setFiles(List.of());
    }

    @Test
    void createSession() throws Exception {
        Mockito.when(sessionService.create()).thenReturn(testSession);

        mockMvc.perform(post("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("创建任务会话成功"))
                .andExpect(jsonPath("$.data.session_id").value("test-session-123"));
    }

    @Test
    void getAllSessions() throws Exception {
        Mockito.when(sessionService.listAll()).thenReturn(List.of(testSession));

        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sessions", hasSize(1)))
                .andExpect(jsonPath("$.data.sessions[0].session_id").value("test-session-123"))
                .andExpect(jsonPath("$.data.sessions[0].title").value("Test Session"))
                .andExpect(jsonPath("$.data.sessions[0].status").value("pending"));
    }

    @Test
    void getSessionDetail() throws Exception {
        Mockito.when(sessionService.getDetail("test-session-123")).thenReturn(testSession);

        mockMvc.perform(get("/api/v1/sessions/test-session-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.session_id").value("test-session-123"))
                .andExpect(jsonPath("$.data.events").exists())
                .andExpect(jsonPath("$.data.files").exists());
    }

    @Test
    void clearUnreadCount() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/test-session-123/clear-unread-message-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("清除未读消息数成功"));

        Mockito.verify(sessionService).clearUnreadCount("test-session-123");
    }

    @Test
    void deleteSession() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/test-session-123/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("删除任务会话成功"));

        Mockito.verify(sessionService).delete("test-session-123");
    }

    @Test
    void stopSession() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/test-session-123/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("停止任务会话成功"));

        Mockito.verify(sessionService).stop("test-session-123");
    }

    @Test
    void listFiles() throws Exception {
        Mockito.when(sessionService.listFiles("test-session-123"))
                .thenReturn(List.of(FileMeta.ofFilepath("/workspace/result.txt")));

        mockMvc.perform(get("/api/v1/sessions/test-session-123/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.files", hasSize(1)));
    }

    @Test
    void runtimeMetrics() throws Exception {
        Mockito.when(sessionService.getDetail("test-session-123")).thenReturn(testSession);
        Mockito.when(executionObservationSink.getLatestBySessionId("test-session-123"))
                .thenReturn(Optional.of(ExecutionSnapshot.submitted("test-session-123", "task-1").toBuilder()
                        .status("running")
                        .eventCount(3)
                        .toolCallCount(1)
                        .stepStartCount(1)
                        .repeatedToolCallCount(1)
                        .loopDetected(false)
                        .warnings(List.of())
                        .build()));

        mockMvc.perform(get("/api/v1/sessions/test-session-123/runtime-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.session_id").value("test-session-123"))
                .andExpect(jsonPath("$.data.task_id").value("task-1"))
                .andExpect(jsonPath("$.data.status").value("running"));
    }

    @Test
    void chatShouldReturnSse() throws Exception {
        String requestBody = """
                {
                  "message": "Hello"
                }
                """;

        mockMvc.perform(post("/api/v1/sessions/test-session-123/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        Mockito.verify(chatService, timeout(500)).chat(eq("test-session-123"), eq("Hello"), any(), eq(null), eq(null), any());
    }
}
