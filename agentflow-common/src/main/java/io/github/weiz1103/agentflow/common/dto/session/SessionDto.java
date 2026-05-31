package io.github.weiz1103.agentflow.common.dto.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 会话相关 DTO 集合。
 * 所有字段命名与 Python session.py schema 保持一致（JSON 序列化时使用 snake_case）。
 * @author zhuang03@qq.com
 * @date 2026-05-28 23:51:38
 */
public class SessionDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateSessionResponse {
        @JsonProperty("session_id")
        private String sessionId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ListSessionItem {
        @JsonProperty("session_id")
        private String sessionId;
        private String title;
        @JsonProperty("latest_message")
        private String latestMessage;
        @JsonProperty("latest_message_at")
        private LocalDateTime latestMessageAt;
        private String status;
        @JsonProperty("unread_message_count")
        private int unreadMessageCount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ListSessionResponse {
        private List<ListSessionItem> sessions = new ArrayList<>();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GetSessionResponse {
        @JsonProperty("session_id")
        private String sessionId;
        private String title;
        private String status;
        private List<Map<String, Object>> events = new ArrayList<>();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChatRequest {
        private String message;
        private List<String> attachments = new ArrayList<>();
        @JsonProperty("event_id")
        private String eventId;
        private Long timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GetSessionFilesResponse {
        private List<FileItem> files = new ArrayList<>();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileItem {
        private String id;
        private String name;
        private String path;
        private String extension;
        private long size;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileReadRequest {
        private String filepath;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileReadResponse {
        private String filepath;
        private String content;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShellReadRequest {
        @JsonProperty("session_id")
        private String sessionId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ConsoleRecord {
        private String ps1;
        private String command;
        private String output;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShellReadResponse {
        @JsonProperty("session_id")
        private String sessionId;
        private String output;
        @JsonProperty("console_records")
        private List<ConsoleRecord> consoleRecords = new ArrayList<>();
    }
}

