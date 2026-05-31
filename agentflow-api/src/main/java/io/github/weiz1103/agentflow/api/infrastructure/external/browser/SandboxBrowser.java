package io.github.weiz1103.agentflow.api.infrastructure.external.browser;

import io.github.weiz1103.agentflow.api.domain.external.Browser;
import io.github.weiz1103.agentflow.api.domain.model.toolresult.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Base64;
import java.util.Map;

/**
 * 基于沙箱HTTP API的浏览器实现。
 * <p>
 * 通过调用沙箱服务（manus-sandbox-server）的浏览器API实现各种操作。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-26 22:53:25
 */
public class SandboxBrowser implements Browser {

    private static final Logger logger = LoggerFactory.getLogger(SandboxBrowser.class);

    private final RestClient client;
    private final String sessionId;

    /**
     * 构造函数，完成浏览器管理器的初始化
     *
     * @param cdpUrl CDP地址（用于构建sandbox API的baseUrl。
     */
    public SandboxBrowser(String cdpUrl) {
        this(cdpUrl, "default");
    }

    public SandboxBrowser(String cdpUrl, String sessionId) {
        String sandboxBaseUrl = normalizeSandboxBaseUrl(cdpUrl);
        this.client = RestClient.builder().baseUrl(sandboxBaseUrl).build();
        this.sessionId = (sessionId == null || sessionId.isBlank()) ? "default" : sessionId;
    }

    @Override
    public ToolResult<Object> viewPage() {
        return getFromBrowser("/api/browser/" + sessionId + "/view");
    }

    @Override
    public ToolResult<Object> navigate(String url) {
        return postToBrowser("/api/browser/navigate", Map.of("sessionId", sessionId, "url", url));
    }

    @Override
    public ToolResult<Object> restart(String url) {
        return navigate(url);
    }

    @Override
    public ToolResult<Object> click(Integer index, Double coordinateX, Double coordinateY) {
        if (index == null) {
            return ToolResult.fail("当前 Java 沙箱浏览器点击仅支持按元。index 操作");
        }
        return postToBrowser("/api/browser/click", Map.of("sessionId", sessionId, "index", index));
    }

    @Override
    public ToolResult<Object> input(String text, boolean pressEnter, Integer index,
                                     Double coordinateX, Double coordinateY) {
        if (index == null) {
            return ToolResult.fail("当前 Java 沙箱浏览器输入仅支持按元。index 操作");
        }
        return postToBrowser("/api/browser/type", Map.of(
                "sessionId", sessionId,
                "index", index,
                "text", text,
                "pressEnter", pressEnter
        ));
    }

    @Override
    public ToolResult<Object> moveMouse(double coordinateX, double coordinateY) {
        return ToolResult.fail("当前 Java 沙箱浏览器暂未实。moveMouse 接口");
    }

    @Override
    public ToolResult<Object> pressKey(String key) {
        return ToolResult.fail("当前 Java 沙箱浏览器暂未实。pressKey 接口");
    }

    @Override
    public ToolResult<Object> selectOption(int index, int option) {
        return ToolResult.fail("当前 Java 沙箱浏览器暂未实。selectOption 接口");
    }

    @Override
    public ToolResult<Object> scrollUp(Boolean toTop) {
        return ToolResult.fail("当前 Java 沙箱浏览器暂未实。scrollUp 接口");
    }

    @Override
    public ToolResult<Object> scrollDown(Boolean toDown) {
        return ToolResult.fail("当前 Java 沙箱浏览器暂未实。scrollDown 接口");
    }

    @Override
    public byte[] screenshot(Boolean fullPage) {
        try {
            ToolResult<Object> viewResult = viewPage();
            if (!viewResult.success()) {
                return new byte[0];
            }
            if (viewResult.data() instanceof Map<?, ?> map) {
                Object screenshot = map.get("screenshot");
                if (screenshot != null) {
                    return Base64.getDecoder().decode(String.valueOf(screenshot));
                }
            }
            return new byte[0];
        } catch (Exception e) {
            logger.error("浏览器截图失。 {}", e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public ToolResult<Object> consoleExec(String javascript) {
        return ToolResult.fail("当前 Java 沙箱浏览器暂未实。consoleExec 接口");
    }

    @Override
    public ToolResult<Object> consoleView(Integer maxLines) {
        return ToolResult.fail("当前 Java 沙箱浏览器暂未实。consoleView 接口");
    }

    /**
     * 通用POST到浏览器API
     */
    @SuppressWarnings("unchecked")
    private ToolResult<Object> postToBrowser(String uri, Object body) {
        try {
            Map<String, Object> response = client.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (response == null) return ToolResult.fail("浏览器API返回空响。);
            int code = ((Number) response.getOrDefault("code", 500)).intValue();
            String msg = String.valueOf(response.getOrDefault("msg", response.getOrDefault("message", "")));
            Object data = response.get("data");
            return ToolResult.fromSandbox(code, msg, data);
        } catch (Exception e) {
            logger.error("调用浏览器API[{}]失败: {}", uri, e.getMessage());
            return ToolResult.fail("调用浏览器API失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ToolResult<Object> getFromBrowser(String uri) {
        try {
            Map<String, Object> response = client.get()
                    .uri(uri)
                    .retrieve()
                    .body(Map.class);
            if (response == null) return ToolResult.fail("浏览器API返回空响。);
            int code = ((Number) response.getOrDefault("code", 500)).intValue();
            String msg = String.valueOf(response.getOrDefault("msg", response.getOrDefault("message", "")));
            Object data = response.get("data");
            return ToolResult.fromSandbox(code, msg, data);
        } catch (Exception e) {
            logger.error("调用浏览器API[{}]失败: {}", uri, e.getMessage());
            return ToolResult.fail("调用浏览器API失败: " + e.getMessage());
        }
    }

    private static String normalizeSandboxBaseUrl(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return "http://localhost:8080";
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            if (trimmed.endsWith(":9222")) {
                return trimmed.replace(":9222", ":8080");
            }
            return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        }
        URI uri = URI.create("http://" + trimmed);
        return uri.toString().endsWith("/") ? uri.toString().substring(0, uri.toString().length() - 1) : uri.toString();
    }
}


