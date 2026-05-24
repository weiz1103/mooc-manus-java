package com.imooc.manus.common.dto.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class FileDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileReadRequest {
        private String filepath;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileReadResult {
        private String filepath;
        private String content;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileWriteRequest {
        private String filepath;
        private String content;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileListResult {
        private String directory;
        @JsonProperty("file_count")
        private int fileCount;
        private java.util.List<FileInfo> files;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FileInfo {
        private String name;
        private String path;
        private String extension;
        private long size;
        private boolean isDirectory;
    }
}
