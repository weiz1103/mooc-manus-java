package com.imooc.manus.springai.config;

import com.imooc.manus.springai.domain.port.*;
import com.imooc.manus.springai.memory.AgentMemoryStore;
import com.imooc.manus.springai.memory.InMemoryAgentMemoryStore;
import com.imooc.manus.springai.session.InMemorySessionStateLoader;
import com.imooc.manus.springai.session.SessionStateLoader;
import com.imooc.manus.springai.tool.*;
import com.imooc.manus.springai.tool.dynamic.A2AToolCallbackProvider;
import com.imooc.manus.springai.tool.dynamic.McpToolCallbackProvider;
import com.imooc.manus.springai.tool.registry.ToolCallbackRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * manus-spring-ai 模块的 Spring Boot 自动配置。
 * <p>
 * 遵循 Spring Boot 条件装配原则：
 * <ul>
 *   <li>基础组件（内存实现）使用 {@code @ConditionalOnMissingBean} 提供默认实现，
 *       方便 manus-api 层替换为持久化实现。</li>
 *   <li>工具服务（FileToolService 等）在对应的 Port Bean 存在时自动注册。</li>
 *   <li>ToolCallbackRegistry 汇总所有工具，供 SpringAIPlannerReActFlow 使用。</li>
 * </ul>
 * </p>
 *
 * <h3>扩展方式</h3>
 * <ol>
 *   <li>在 manus-api 层注入 {@link SandboxPort}、{@link BrowserPort}、
 *       {@link SearchEnginePort} 的具体实现 Bean，本配置将自动生效。</li>
 *   <li>如需替换内存实现，在 manus-api 中注册相同类型的 Bean（
 *       {@link AgentMemoryStore} / {@link SessionStateLoader}）即可。</li>
 *   <li>如需禁用某个工具，在 manus-api 中排除对应的 Port Bean 即可。</li>
 * </ol>
 */
@Configuration
@EnableConfigurationProperties(ManusAgentProperties.class)
public class ManusSpringAIAutoConfiguration {

    // ─────────────────────────────────────────────
    // 默认基础设施实现（可被 manus-api 替换）
    // ─────────────────────────────────────────────

    /**
     * 默认内存 AgentMemoryStore（生产环境由 manus-api 替换为 JPA 实现）。
     */
    @Bean
    @ConditionalOnMissingBean(AgentMemoryStore.class)
    public AgentMemoryStore agentMemoryStore() {
        return new InMemoryAgentMemoryStore();
    }

    /**
     * 默认内存 SessionStateLoader（生产环境由 manus-api 替换为 JPA 实现）。
     */
    @Bean
    @ConditionalOnMissingBean(SessionStateLoader.class)
    public SessionStateLoader sessionStateLoader() {
        return new InMemorySessionStateLoader();
    }

    // ─────────────────────────────────────────────
    // 工具服务（依赖对应 Port Bean 注入）
    // ─────────────────────────────────────────────

    @Bean
    @ConditionalOnBean(SandboxPort.class)
    @ConditionalOnMissingBean(FileToolService.class)
    public FileToolService fileToolService(SandboxPort sandbox) {
        return new FileToolService(sandbox);
    }

    @Bean
    @ConditionalOnBean(SandboxPort.class)
    @ConditionalOnMissingBean(ShellToolService.class)
    public ShellToolService shellToolService(SandboxPort sandbox) {
        return new ShellToolService(sandbox);
    }

    @Bean
    @ConditionalOnBean(BrowserPort.class)
    @ConditionalOnMissingBean(BrowserToolService.class)
    public BrowserToolService browserToolService(BrowserPort browser) {
        return new BrowserToolService(browser);
    }

    @Bean
    @ConditionalOnBean(SearchEnginePort.class)
    @ConditionalOnMissingBean(SearchToolService.class)
    public SearchToolService searchToolService(SearchEnginePort searchEngine) {
        return new SearchToolService(searchEngine);
    }

    @Bean
    @ConditionalOnMissingBean(MessageToolService.class)
    public MessageToolService messageToolService() {
        return new MessageToolService();
    }

    // ─────────────────────────────────────────────
    // 动态工具提供者（可选，有 MCP/A2A Port 时才注册）
    // ─────────────────────────────────────────────

    @Bean
    @ConditionalOnBean(McpPort.class)
    @ConditionalOnMissingBean(McpToolCallbackProvider.class)
    public McpToolCallbackProvider mcpToolCallbackProvider(McpPort mcpPort) {
        return new McpToolCallbackProvider(mcpPort);
    }

    @Bean
    @ConditionalOnBean(A2APort.class)
    @ConditionalOnMissingBean(A2AToolCallbackProvider.class)
    public A2AToolCallbackProvider a2aToolCallbackProvider(A2APort a2aPort) {
        return new A2AToolCallbackProvider(a2aPort);
    }

    // ─────────────────────────────────────────────
    // 工具注册表（汇总所有工具）
    // ─────────────────────────────────────────────

    /**
     * 工具回调注册表 Bean。
     * <p>
     * 注意：至少需要 FileToolService + ShellToolService + BrowserToolService +
     * SearchToolService + MessageToolService 才能完整注册（各 Port 实现注入后自动生效）。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(ToolCallbackRegistry.class)
    @ConditionalOnBean({
            FileToolService.class,
            ShellToolService.class,
            BrowserToolService.class,
            SearchToolService.class,
            MessageToolService.class
    })
    public ToolCallbackRegistry toolCallbackRegistry(
            FileToolService    fileToolService,
            ShellToolService   shellToolService,
            BrowserToolService browserToolService,
            SearchToolService  searchToolService,
            MessageToolService messageToolService,
            // 可选依赖
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            McpToolCallbackProvider mcpProvider,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            A2AToolCallbackProvider a2aProvider
    ) {
        ToolCallbackRegistry registry = new ToolCallbackRegistry(
                fileToolService, shellToolService, browserToolService,
                searchToolService, messageToolService);
        if (mcpProvider != null) registry.withMcp(mcpProvider);
        if (a2aProvider != null) registry.withA2A(a2aProvider);
        return registry;
    }
}

