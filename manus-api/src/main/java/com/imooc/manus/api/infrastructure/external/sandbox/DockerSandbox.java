package com.imooc.manus.api.infrastructure.external.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.imooc.manus.api.domain.external.Browser;
import com.imooc.manus.api.domain.external.Sandbox;
import com.imooc.manus.api.domain.model.toolresult.ToolResult;
import com.imooc.manus.api.infrastructure.config.AppProperties;
import com.imooc.manus.api.infrastructure.external.browser.SandboxBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 基于Docker的沙箱服务。
 * <p>
 * Java manus-sandbox-server），完成文件、Shell、浏览器等操作。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-26 02:58:48
 */
public class DockerSandbox implements Sandbox {

    private static final Logger logger = LoggerFactory.getLogger(DockerSandbox.class);
    private final RestClient client;
    private final String ip;
    private final String containerName;
    private final String baseUrl;
    private final String vncUrl;
    private final String cdpUrl;
    private final Browser browser;

    /**
     * 构造函数，完成Docker沙箱扩展创建
     *
     * @param ip            沙箱IP地址
     * @param containerName Docker容器名字（可为null）
     */
    public DockerSandbox(String ip, String containerName) {
        URI sandboxUri = normalizeSandboxUri(ip);
        this.ip = sandboxUri.getHost() != null ? sandboxUri.getHost() : ip;
        this.containerName = containerName;
        this.baseUrl = stripTrailingSlash(sandboxUri.toString());
        this.vncUrl = "ws://" + this.ip + ":5901";
        this.cdpUrl = "http://" + this.ip + ":9222";
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.browser = new SandboxBrowser(baseUrl, getId());
    }

    /**
     * 根据应用配置创建沙箱实例（类方法）
     *
     * @param props 应用配置
     * @return 沙箱实例
     */
    public static DockerSandbox createFromConfig(AppProperties props) {
        String sandboxAddress = props.getSandbox().getAddress();

        if (sandboxAddress != null && !sandboxAddress.isBlank()) {
            // 1.使用现成的沙箱地址
            return new DockerSandbox(sandboxAddress, null);
        }

        // 2.使用Docker创建新容器
        return createTask(props);
    }

    /**
     * 根据传递的id获取沙箱实例（类方法）
     *
     * @param id    沙箱id（容器名字）
     * @param props 应用配置
     * @return 沙箱实例（Optional）
     */
    public static Optional<DockerSandbox> getById(String id, AppProperties props) {
        String sandboxAddress = props.getSandbox().getAddress();

        if (sandboxAddress != null && !sandboxAddress.isBlank()) {
            return Optional.of(new DockerSandbox(sandboxAddress, id));
        }

        try {
            // 1.创建docker客户端并根据容器名字获取容器
            DockerClient dockerClient = buildDockerClient();
            List<Container> containers = dockerClient.listContainersCmd()
                    .withNameFilter(List.of(id))
                    .withShowAll(false)
                    .exec();

            dockerClient.close();

            if (containers.isEmpty()) {
                logger.warn("该容器找不到可能被销毁: {}", id);
                return Optional.empty();
            }

            Container container = containers.get(0);
            String ip = getContainerIp(container);
            if (ip == null || ip.isBlank()) return Optional.empty();

            return Optional.of(new DockerSandbox(ip, id));
        } catch (Exception e) {
            logger.error("获取沙箱发生未知错误: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 创建沙箱容器的任务（类方法）
     *
     * @param props 应用配置
     * @return 新创建的沙箱实例
     */
    private static DockerSandbox createTask(AppProperties props) {
        AppProperties.SandboxProperties sandboxProps = props.getSandbox();
        String image = sandboxProps.getImage();
        String namePrefix = sandboxProps.getNamePrefix();
        String containerName = namePrefix + "-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            DockerClient dockerClient = buildDockerClient();
            var createCmd = dockerClient.createContainerCmd(image)
                    .withName(containerName);

            // 配置环境变量
            List<String> envList = new ArrayList<>();
            envList.add("SERVICE_TIMEOUT_MINUTES=" + sandboxProps.getTtlMinutes());
            if (sandboxProps.getChromeArgs() != null) envList.add("CHROME_ARGS=" + sandboxProps.getChromeArgs());
            if (sandboxProps.getHttpsProxy() != null) envList.add("HTTPS_PROXY=" + sandboxProps.getHttpsProxy());
            if (sandboxProps.getHttpProxy() != null) envList.add("HTTP_PROXY=" + sandboxProps.getHttpProxy());
            if (sandboxProps.getNoProxy() != null) envList.add("NO_PROXY=" + sandboxProps.getNoProxy());
            createCmd.withEnv(envList);

            // 配置网络
            if (sandboxProps.getNetwork() != null && !sandboxProps.getNetwork().isBlank()) {
                // network配置
                createCmd.withHostConfig(com.github.dockerjava.api.model.HostConfig.newHostConfig()
                        .withNetworkMode(sandboxProps.getNetwork())
                        .withAutoRemove(true));
            }

            String containerId = createCmd.exec().getId();
            dockerClient.startContainerCmd(containerId).exec();

            // 获取容器IP
            var inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
            String ip = extractIpFromInspect(inspectResponse);
            dockerClient.close();

            logger.info("创建Docker沙箱容器成功: {}, IP: {}", containerName, ip);
            return new DockerSandbox(ip, containerName);
        } catch (Exception e) {
            logger.error("创建Docker沙箱容器失败: {}", e.getMessage());
            throw new RuntimeException("创建Docker沙箱容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从容器对象获取IP地址
     */
    private static String getContainerIp(Container container) {
        if (container.getNetworkSettings() == null) return null;
        Map<String, com.github.dockerjava.api.model.ContainerNetwork> networks =
                container.getNetworkSettings().getNetworks();
        if (networks == null || networks.isEmpty()) return null;
        for (Map.Entry<String, com.github.dockerjava.api.model.ContainerNetwork> entry : networks.entrySet()) {
            String ip = entry.getValue().getIpAddress();
            if (ip != null && !ip.isBlank()) return ip;
        }
        return null;
    }

    /**
     * 从inspect响应中提取IP地址
     */
    private static String extractIpFromInspect(com.github.dockerjava.api.command.InspectContainerResponse inspect) {
        if (inspect.getNetworkSettings() == null) return "";
        String ip = inspect.getNetworkSettings().getIpAddress();
        if (ip != null && !ip.isBlank()) return ip;

        Map<String, com.github.dockerjava.api.model.ContainerNetwork> networks =
                inspect.getNetworkSettings().getNetworks();
        if (networks != null) {
            for (Map.Entry<String, com.github.dockerjava.api.model.ContainerNetwork> entry : networks.entrySet()) {
                String networkIp = entry.getValue().getIpAddress();
                if (networkIp != null && !networkIp.isBlank()) return networkIp;
            }
        }
        return "";
    }

    /**
     * 构建Docker客户端
     */
    private static DockerClient buildDockerClient() {
        var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }

    // ======================== Sandbox接口实现（HTTP调用沙箱服务） ========================

    @Override
    public String getId() {
        return containerName != null ? containerName : "mooc-manus-sandbox";
    }

    @Override
    public String getVncUrl() { return vncUrl; }

    @Override
    public String getCdpUrl() { return cdpUrl; }

    @Override
    public Browser getBrowser() {
        return browser;
    }

    /**
     * 确保沙箱存在，循环等待所有服务启动完成。
     */
    @Override
    @SuppressWarnings("unchecked")
    public void ensureSandbox() {
        int maxRetries = 15;
        int retryIntervalMs = 1000;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                Map<String, Object> response = client.get()
                        .uri("/api/supervisor/health")
                        .retrieve()
                        .body(Map.class);

                if (response != null) {
                    int code = ((Number) response.getOrDefault("code", 500)).intValue();
                    if (code < 300) {
                        logger.info("Sandbox健康检查通过: {}", baseUrl);
                        return;
                    }
                }
            } catch (Exception e) {
                logger.warn("Sandbox健康检查失败（第{}次）: {}", attempt + 1, e.getMessage());
            }
            sleep(retryIntervalMs);
        }

        throw new RuntimeException("无法连接沙箱服务: " + baseUrl);
    }

    @Override
    public boolean destroy() {
        try {
            if (containerName != null) {
                DockerClient dockerClient = buildDockerClient();
                dockerClient.removeContainerCmd(containerName).withForce(true).exec();
                dockerClient.close();
            }
            return true;
        } catch (Exception e) {
            logger.error("销毁当前Docker沙箱[{}]失败: {}", containerName, e.getMessage());
            return false;
        }
    }

    // ======================= 文件操作（HTTP调用沙箱API）=======================

    @Override
    public ToolResult<Object> readFile(String filepath, Integer startLine, Integer endLine, boolean sudo, int maxLength) {
        return postToSandbox("/api/file/read", Map.of("filepath", filepath));
    }

    @Override
    public ToolResult<Object> writeFile(String filepath, String content, boolean append,
                                         boolean leadingNewline, boolean trailingNewline, boolean sudo) {
        String finalContent = content != null ? content : "";

        if (leadingNewline) {
            finalContent = System.lineSeparator() + finalContent;
        }
        if (trailingNewline) {
            finalContent = finalContent + System.lineSeparator();
        }
        if (append) {
            ToolResult<Object> current = readFile(filepath, null, null, sudo, Integer.MAX_VALUE);
            Object currentData = current.data();
            String currentContent = extractFileContent(currentData);
            finalContent = currentContent + finalContent;
        }

        return postToSandbox("/api/file/write", Map.of(
                "filepath", filepath,
                "content", finalContent
        ));
    }

    @Override
    public ToolResult<Object> replaceInFile(String filepath, String oldStr, String newStr, boolean sudo) {
        ToolResult<Object> current = readFile(filepath, null, null, sudo, Integer.MAX_VALUE);
        if (!current.success()) {
            return current;
        }
        String currentContent = extractFileContent(current.data());
        String replaced = currentContent.replace(oldStr, newStr);
        return writeFile(filepath, replaced, false, false, false, sudo);
    }

    @Override
    public ToolResult<Object> searchInFile(String filepath, String regex, boolean sudo) {
        ToolResult<Object> current = readFile(filepath, null, null, sudo, Integer.MAX_VALUE);
        if (!current.success()) {
            return current;
        }
        String currentContent = extractFileContent(current.data());
        Pattern pattern = Pattern.compile(regex);
        List<Map<String, Object>> matches = new ArrayList<>();
        String[] lines = currentContent.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            if (pattern.matcher(lines[i]).find()) {
                matches.add(Map.of("line", i + 1, "content", lines[i]));
            }
        }
        return ToolResult.ok(Map.of("filepath", filepath, "matches", matches));
    }

    @Override
    public ToolResult<Object> findFiles(String dirPath, String globPattern) {
        ToolResult<Object> listResult = getFromSandbox(uriBuilder -> uriBuilder.path("/api/file/list")
                .queryParam("directory", dirPath)
                .build());
        if (!listResult.success()) {
            return listResult;
        }
        Object data = listResult.data();
        if (!(data instanceof List<?> items)) {
            return ToolResult.ok(List.of());
        }
        java.nio.file.PathMatcher matcher = java.nio.file.FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        List<Object> filtered = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                Object name = map.get("name");
                if (name != null && matcher.matches(java.nio.file.Path.of(String.valueOf(name)))) {
                    filtered.add(map);
                }
            }
        }
        return ToolResult.ok(filtered);
    }

    @Override
    public ToolResult<Object> listFiles(String dirPath) {
        return findFiles(dirPath, "*");
    }

    @Override
    public ToolResult<Object> checkFileExists(String filepath) {
        String normalized = filepath.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String directory = lastSlash >= 0 ? normalized.substring(0, lastSlash) : ".";
        String filename = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        ToolResult<Object> files = findFiles(directory, filename);
        if (!files.success()) {
            return files;
        }
        boolean exists = files.data() instanceof List<?> list && !list.isEmpty();
        return ToolResult.ok(Map.of("filepath", filepath, "exists", exists));
    }

    @Override
    public ToolResult<Object> deleteFile(String filepath) {
        return ToolResult.fail("当前 Java 沙箱服务暂未实现 deleteFile 接口");
    }

    @Override
    public ToolResult<Object> uploadFile(InputStream fileData, String filepath, String filename) {
        return ToolResult.fail("当前 Java 沙箱服务暂未实现 uploadFile 接口");
    }

    @Override
    public InputStream downloadFile(String filepath) {
        logger.warn("当前 Java 沙箱服务暂未实现 downloadFile 接口: {}", filepath);
        return InputStream.nullInputStream();
    }

    // ======================= Shell操作 =======================

    @Override
    public ToolResult<Object> execCommand(String sessionId, String execDir, String command) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("session_id", sessionId);
        if (execDir != null && !execDir.isBlank()) {
            body.put("exec_dir", execDir);
        }
        body.put("command", command);
        return postToSandbox("/api/shell/exec", body);
    }

    @Override
    public ToolResult<Object> readShellOutput(String sessionId, boolean console) {
        return getFromSandbox("/api/shell/" + sessionId + "/output");
    }

    @Override
    public ToolResult<Object> waitProcess(String sessionId, Integer seconds) {
        return readShellOutput(sessionId, false);
    }

    @Override
    public ToolResult<Object> writeShellInput(String sessionId, String inputText, boolean pressEnter) {
        return ToolResult.fail("当前 Java 沙箱服务暂未实现交互式 writeShellInput 接口");
    }

    @Override
    public ToolResult<Object> killProcess(String sessionId) {
        try {
            Map<String, Object> response = client.post()
                    .uri("/api/shell/{sessionId}/kill", sessionId)
                    .retrieve()
                    .body(Map.class);
            return fromSandboxResponse(response);
        } catch (Exception e) {
            logger.error("终止沙箱进程失败: {}", e.getMessage());
            return ToolResult.fail("终止沙箱进程失败: " + e.getMessage());
        }
    }

    // ======================= 辅助方法 =======================

    /**
     * 通用POST请求到沙箱API
     */
    @SuppressWarnings("unchecked")
    private ToolResult<Object> postToSandbox(String uri, Map<String, Object> body) {
        try {
            Map<String, Object> response = client.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return fromSandboxResponse(response);
        } catch (Exception e) {
            logger.error("调用沙箱API[{}]失败: {}", uri, e.getMessage());
            return ToolResult.fail("调用沙箱API失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ToolResult<Object> getFromSandbox(String uri) {
        try {
            Map<String, Object> response = client.get()
                    .uri(uri)
                    .retrieve()
                    .body(Map.class);
            return fromSandboxResponse(response);
        } catch (Exception e) {
            logger.error("调用沙箱API[{}]失败: {}", uri, e.getMessage());
            return ToolResult.fail("调用沙箱API失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ToolResult<Object> getFromSandbox(java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI> uriFunction) {
        try {
            Map<String, Object> response = client.get()
                    .uri(uriFunction)
                    .retrieve()
                    .body(Map.class);
            return fromSandboxResponse(response);
        } catch (Exception e) {
            logger.error("调用沙箱API失败: {}", e.getMessage());
            return ToolResult.fail("调用沙箱API失败: " + e.getMessage());
        }
    }

    /**
     * 将沙箱响应转换为ToolResult
     */
    @SuppressWarnings("unchecked")
    private ToolResult<Object> fromSandboxResponse(Map<String, Object> response) {
        if (response == null) return ToolResult.fail("沙箱返回空响应");
        int code = ((Number) response.getOrDefault("code", 500)).intValue();
        String msg = String.valueOf(response.getOrDefault("msg", response.getOrDefault("message", "")));
        Object data = response.get("data");
        return ToolResult.fromSandbox(code, msg, data);
    }

    private static URI normalizeSandboxUri(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return URI.create("http://localhost:8080");
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return URI.create(trimmed);
        }
        if (trimmed.matches("^[^/]+:\\d+$")) {
            return URI.create("http://" + trimmed);
        }
        return URI.create("http://" + trimmed + ":8080");
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String extractFileContent(Object data) {
        if (data instanceof Map<?, ?> map) {
            Object content = map.get("content");
            return content != null ? String.valueOf(content) : "";
        }
        return data != null ? String.valueOf(data) : "";
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

