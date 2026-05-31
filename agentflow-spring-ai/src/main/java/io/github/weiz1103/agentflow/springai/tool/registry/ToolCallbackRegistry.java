package io.github.weiz1103.agentflow.springai.tool.registry;

import io.github.weiz1103.agentflow.springai.tool.*;
import io.github.weiz1103.agentflow.springai.tool.dynamic.A2AToolCallbackProvider;
import io.github.weiz1103.agentflow.springai.tool.dynamic.McpToolCallbackProvider;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.util.*;

/**
 * 工具回调注册表（中央收集器）。
 * <p>
 * 负责聚合所有静态工具（{@code @Tool} 注解服务）和动态工具（MCP / A2A）的
 * {@link ToolCallback}，供 {@code SpringAIPlannerReActFlow} 。
 * {@code BaseSpringAIAgent} 统一获取。
 * </p>
 *
 * <h3>命名规范</h3>
 * <p>
 * 现有 {@code BaseSpringAIAgent} 。{@code "message_ask_user"} 等工具名有硬编码判断
 * （用于触。WaitEvent），因此消息类工具必须保。snake_case 命名。
 * 文件、Shell、浏览器、搜索等工具使用 Spring AI {@code @Tool(name = "...")}
 * 显式声明。snake_case 名称，运行时。Python 兼容的函数调用名称匹配。
 * </p>
 *
 * <h3>扩展。/h3>
 * <ul>
 *   <li>新增静态工具：创建新的 {@code *ToolService} 并在构造器中注入，
 *       调用 {@code ToolCallbacks.from(service)} 添加。list。</li>
 *   <li>新增动态工具提供者：实现新的 {@code *ToolCallbackProvider}。
 *       。{@link #buildAll()} 中调用其 {@code buildToolCallbacks()}。</li>
 *   <li>覆盖消息工具名称：修。{@link #buildMessageToolCallbacks()} 即可。</li>
 * </ul>
 * @author zhuang03@qq.com
 * @date 2026-05-31 20:15:13
 */
public class ToolCallbackRegistry {

    private final FileToolService    fileToolService;
    private final ShellToolService   shellToolService;
    private final BrowserToolService browserToolService;
    private final SearchToolService  searchToolService;
    private final MessageToolService messageToolService;

    /** 可选：MCP 动态工具提供者（。null 时跳过） */
    private McpToolCallbackProvider mcpProvider;

    /** 可选：A2A 动态工具提供者（。null 时跳过） */
    private A2AToolCallbackProvider a2aProvider;

    public ToolCallbackRegistry(
            FileToolService    fileToolService,
            ShellToolService   shellToolService,
            BrowserToolService browserToolService,
            SearchToolService  searchToolService,
            MessageToolService messageToolService
    ) {
        this.fileToolService    = fileToolService;
        this.shellToolService   = shellToolService;
        this.browserToolService = browserToolService;
        this.searchToolService  = searchToolService;
        this.messageToolService = messageToolService;
    }

    public ToolCallbackRegistry withMcp(McpToolCallbackProvider mcpProvider) {
        this.mcpProvider = mcpProvider;
        return this;
    }

    public ToolCallbackRegistry withA2A(A2AToolCallbackProvider a2aProvider) {
        this.a2aProvider = a2aProvider;
        return this;
    }

    /**
     * 构建所有工具的 ToolCallback 列表。
     * 每次调用都会重新构建，以。MCP 动态工具能够反映最新状态。
     *
     * @return 不可变的 ToolCallback 列表
     */
    public List<ToolCallback> buildAll() {
        List<ToolCallback> list = new ArrayList<>();

        // 1. 静态工具（@Tool 注解方法，通过 ToolCallbacks.from 提取，工具名。@Tool(name=...) 显式声明。
        Collections.addAll(list, ToolCallbacks.from(fileToolService));
        Collections.addAll(list, ToolCallbacks.from(shellToolService));
        Collections.addAll(list, ToolCallbacks.from(browserToolService));
        Collections.addAll(list, ToolCallbacks.from(searchToolService));

        // 2. 消息工具：同样通过 @Tool(name=...) 暴露 snake_case 名称。
        //    。BaseSpringAIAgent 的特殊处理逻辑保持一致。
        Collections.addAll(list, ToolCallbacks.from(messageToolService));

        // 3. MCP 动态工。
        if (mcpProvider != null) {
            list.addAll(mcpProvider.buildToolCallbacks());
        }

        // 4. A2A 动态工。
        if (a2aProvider != null) {
            list.addAll(a2aProvider.buildToolCallbacks());
        }

        return Collections.unmodifiableList(list);
    }
}

