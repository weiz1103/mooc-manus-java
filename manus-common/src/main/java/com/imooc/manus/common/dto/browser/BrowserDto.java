package com.imooc.manus.common.dto.browser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-28 18:57:03
 */
public class BrowserDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NavigateRequest {
        private String sessionId;
        private String url;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NavigateResult {
        private String url;
        private String title;
        private String status; // "success" | "error"
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ViewPageResult {
        private String url;
        private String content;
        private List<Map<String, Object>> interactiveElements;
        private String screenshot; // Base64 encoded screenshot
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClickRequest {
        private String sessionId;
        private int index;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TypeRequest {
        private String sessionId;
        private int index;
        private String text;
        private boolean pressEnter;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ActionResult {
        private boolean success;
        private String message;
    }
}
