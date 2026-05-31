package io.github.weiz1103.agentflow.springai.domain.port;

import io.github.weiz1103.agentflow.common.model.ToolResult;

/**
 * 沙箱服务端口（Port / Anti-Corruption Layer）。
 * <p>
 * agentflow-spring-ai 通过此接口与沙箱基础设施解耦，
 * agentflow-api 负责提供具体实现（适配器）并注。Spring 容器。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-30 00:13:01
 */
public interface SandboxPort {

    ToolResult<Object> execCommand(String sessionId, String execDir, String command);

    ToolResult<Object> readShellOutput(String sessionId, boolean console);

    ToolResult<Object> waitProcess(String sessionId, Integer seconds);

    ToolResult<Object> writeShellInput(String sessionId, String inputText, boolean pressEnter);

    ToolResult<Object> killProcess(String sessionId);

    ToolResult<Object> writeFile(String filepath, String content, boolean append,
                                 boolean leadingNewline, boolean trailingNewline, boolean sudo);

    ToolResult<Object> readFile(String filepath, Integer startLine, Integer endLine,
                                boolean sudo, int maxLength);

    ToolResult<Object> replaceInFile(String filepath, String oldStr, String newStr, boolean sudo);

    ToolResult<Object> searchInFile(String filepath, String regex, boolean sudo);

    ToolResult<Object> findFiles(String dirPath, String globPattern);
}


