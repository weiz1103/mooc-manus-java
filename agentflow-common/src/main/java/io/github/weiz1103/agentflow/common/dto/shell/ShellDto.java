package io.github.weiz1103.agentflow.common.dto.shell;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class ShellDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShellExecuteRequest {
        @JsonProperty("session_id")
        private String sessionId;
        @JsonProperty("exec_dir")
        private String execDir;
        private String command;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShellExecuteResult {
        @JsonProperty("session_id")
        private String sessionId;
        private String command;
        private String status;   // "completed" | "running" | "failed"
        @JsonProperty("return_code")
        private Integer returnCode;
        private String output;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShellReadResult {
        @JsonProperty("session_id")
        private String sessionId;
        private String output;
        @JsonProperty("console_records")
        private List<ConsoleRecord> consoleRecords = new ArrayList<>();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ConsoleRecord {
        private String ps1;
        private String command;
        private String output;
    }
}

