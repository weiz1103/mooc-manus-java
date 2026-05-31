package com.imooc.manus.api.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.common.event.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-31 19:46:56
 */
public class SseEventStructureTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void testMessageEventSerialization() throws Exception {
        MessageEvent event = MessageEvent.builder()
                .id("msg-123")
                .role("user")
                .message("Hello World")
                .attachments(List.of("file.txt"))
                .build();
        
        String json = objectMapper.writeValueAsString(event);
        
        // Assert frontend contract fields
        assertThat(json).contains("\"id\":\"msg-123\"");
        assertThat(json).contains("\"type\":\"message\"");
        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"message\":\"Hello World\"");
        assertThat(json).contains("\"attachments\":[\"file.txt\"]");
        assertThat(json).contains("\"id\""); // BaseEvent fields
    }

    @Test
    void testWaitEventSerialization() throws Exception {
        WaitEvent event = WaitEvent.builder()
                .id("wait-123")
                .build();

        String json = objectMapper.writeValueAsString(event);

        assertThat(json).contains("\"type\":\"wait\"");
        assertThat(json).contains("\"id\":\"wait-123\"");
    }

    @Test
    void testDoneEventSerialization() throws Exception {
        DoneEvent event = DoneEvent.builder()
                .id("done-123")
                .build();

        String json = objectMapper.writeValueAsString(event);

        assertThat(json).contains("\"type\":\"done\"");
        assertThat(json).contains("\"id\":\"done-123\"");
    }

    @Test
    void testErrorEventSerialization() throws Exception {
        ErrorEvent event = ErrorEvent.builder()
                .id("error-123")
                .error("Something went wrong")
                .build();

        String json = objectMapper.writeValueAsString(event);

        assertThat(json).contains("\"type\":\"error\"");
        assertThat(json).contains("\"error\":\"Something went wrong\"");
    }
}
