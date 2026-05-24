package com.imooc.manus.api.infrastructure.client;

import com.imooc.manus.api.domain.exception.SandboxCommunicationException;
import com.imooc.manus.api.infrastructure.config.SandboxProperties;
import com.imooc.manus.common.dto.browser.BrowserDto;
import com.imooc.manus.common.dto.file.FileDto;
import com.imooc.manus.common.dto.shell.ShellDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * 沙箱 HTTP 客户端，对应 Python Sandbox 抽象类的具体实现。
 * 通过 HTTP 调用 manus-sandbox-server 的各个接口。
 * 使用 Spring 6 RestClient（同步，由虚拟线程支撑，高性能）。
 */
@Component
@Slf4j
public class SandboxClient {

    private final RestClient restClient;

    public SandboxClient(RestClient.Builder builder, SandboxProperties props) {
        this.restClient = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes());
                    log.error("Sandbox error: {} - {}", res.getStatusCode(), body);
                    throw new SandboxCommunicationException(
                            "Sandbox returned error: " + res.getStatusCode() + " - " + body);
                })
                .build();
    }

    // ==================== Shell API ====================

    public ShellDto.ShellExecuteResult executeCommand(ShellDto.ShellExecuteRequest request) {
        log.debug("Sandbox shell execute: {}", request.getCommand());
        return restClient.post()
                .uri("/api/shell/exec")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ShellDto.ShellReadResult readShellOutput(String sessionId) {
        return restClient.get()
                .uri("/api/shell/{sessionId}/output", sessionId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void killProcess(String sessionId) {
        restClient.post()
                .uri("/api/shell/{sessionId}/kill", sessionId)
                .retrieve()
                .toBodilessEntity();
    }

    // ==================== File API ====================

    public FileDto.FileReadResult readFile(FileDto.FileReadRequest request) {
        return restClient.post()
                .uri("/api/file/read")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void writeFile(FileDto.FileWriteRequest request) {
        restClient.post()
                .uri("/api/file/write")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public List<FileDto.FileInfo> listFiles(String directory) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/file/list")
                        .queryParam("directory", directory).build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // ==================== Browser API ====================

    public BrowserDto.NavigateResult navigate(BrowserDto.NavigateRequest request) {
        return restClient.post()
                .uri("/api/browser/navigate")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public BrowserDto.ViewPageResult viewPage(String sessionId) {
        return restClient.get()
                .uri("/api/browser/{sessionId}/view", sessionId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public BrowserDto.ActionResult click(BrowserDto.ClickRequest request) {
        return restClient.post()
                .uri("/api/browser/click")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public BrowserDto.ActionResult type(BrowserDto.TypeRequest request) {
        return restClient.post()
                .uri("/api/browser/type")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // ==================== Supervisor API ====================

    public boolean isAlive() {
        try {
            restClient.get().uri("/api/supervisor/health").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
