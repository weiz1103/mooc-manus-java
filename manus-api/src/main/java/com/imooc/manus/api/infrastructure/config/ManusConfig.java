package com.imooc.manus.api.infrastructure.config;

import com.imooc.manus.api.domain.external.*;
import com.imooc.manus.api.domain.model.appconfig.A2AConfig;
import com.imooc.manus.api.domain.model.appconfig.AgentConfig;
import com.imooc.manus.api.domain.model.appconfig.AppConfig;
import com.imooc.manus.api.domain.model.appconfig.LLMConfig;
import com.imooc.manus.api.domain.model.appconfig.MCPConfig;
import com.imooc.manus.api.domain.repository.IUnitOfWork;
import com.imooc.manus.api.infrastructure.external.jsonparser.RepairJsonParser;
import com.imooc.manus.api.infrastructure.external.llm.OpenAILLM;
import com.imooc.manus.api.infrastructure.external.search.BingSearchEngine;
import com.imooc.manus.api.infrastructure.external.sandbox.DockerSandbox;
import com.imooc.manus.api.infrastructure.external.task.RedisTaskDispatchQueue;
import com.imooc.manus.api.infrastructure.repository.*;
import com.imooc.manus.api.infrastructure.repository.jpa.JpaSessionRepository;
import com.imooc.manus.api.infrastructure.springai.JpaAgentMemoryStore;
import com.imooc.manus.api.infrastructure.springai.JpaSessionStateLoader;
import com.imooc.manus.springai.config.SpringAIFlowConfig;
import com.imooc.manus.springai.domain.port.BrowserPort;
import com.imooc.manus.springai.domain.port.SandboxPort;
import com.imooc.manus.springai.domain.port.SearchEnginePort;
import com.imooc.manus.springai.flow.SpringAIPlannerReActFlow;
import com.imooc.manus.springai.memory.AgentMemoryStore;
import com.imooc.manus.springai.session.SessionStateLoader;
import com.imooc.manus.springai.tool.registry.ToolCallbackRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.function.Supplier;

/**
 * Spring Boot应用核心配置类。
 * <p>
 * 对应Python中的 dependency_injection.py，负责将所有依赖项wire在一起。
 * 遵循构造器注入原则，不使用@Autowired字段注入。
 * </p>
 *
 * @author thezehui@gmail.com
 */
@Configuration
public class ManusConfig {

    /**
     * 从 application.yml 组装的 AppConfig。
     * 彻底移除对旧 Python api/config.yaml 的启动期依赖。
     */
    @Bean
    public AppConfig appConfig(AppProperties appProperties) {
        AppConfig appConfig = new AppConfig();

        AppProperties.LlmProperties llmProperties = appProperties.getLlm();
        LLMConfig llmConfig = new LLMConfig();
        llmConfig.setBaseUrl(llmProperties.getBaseUrl());
        llmConfig.setApiKey(llmProperties.getApiKey());
        llmConfig.setModelName(llmProperties.getModelName());
        llmConfig.setTemperature(llmProperties.getTemperature());
        llmConfig.setMaxTokens(llmProperties.getMaxTokens());
        appConfig.setLlmConfig(llmConfig);

        AppProperties.AgentProperties agentProperties = appProperties.getAgent();
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setMaxIterations(agentProperties.getMaxIterations());
        agentConfig.setMaxRetries(agentProperties.getMaxRetries());
        agentConfig.setMaxSearchResults(agentProperties.getMaxSearchResults());
        appConfig.setAgentConfig(agentConfig);

        MCPConfig mcpConfig = new MCPConfig();
        mcpConfig.setMcpServers(appProperties.getMcp().getServers());
        appConfig.setMcpConfig(mcpConfig);

        A2AConfig a2aConfig = new A2AConfig();
        a2aConfig.setA2aServers(appProperties.getA2a().getServers());
        appConfig.setA2aConfig(a2aConfig);

        return appConfig;
    }

    /**
     * LLM实例
     */
    @Bean
    public LLM llm(AppConfig appConfig) {
        LLMConfig llmConfig = appConfig.getLlmConfig();
        if (llmConfig == null) {
            llmConfig = new LLMConfig();
        }
        return new OpenAILLM(llmConfig);
    }

    /**
     * JSON解析器
     */
    @Bean
    public JsonParser jsonParser() {
        return new RepairJsonParser();
    }

    /**
     * 搜索引擎
     */
    @Bean
    public SearchEngine searchEngine() {
        return new BingSearchEngine();
    }

    @Bean
    @Lazy
    public Sandbox sandbox(AppProperties appProperties) {
        return DockerSandbox.createFromConfig(appProperties);
    }

    @Bean
    @Lazy
    public Browser browser(@Lazy Sandbox sandbox) {
        return sandbox.getBrowser();
    }

    /**
     * 文件仓库
     */
    @Bean
    public com.imooc.manus.api.domain.repository.FileRepository fileRepository() {
        return new InMemoryFileRepository();
    }

    /**
     * 会话仓库
     */
    @Bean
    public com.imooc.manus.api.domain.repository.SessionRepository sessionRepository(
            JpaSessionRepository jpaSessionRepository) {
        return new JpaSessionRepositoryImpl(jpaSessionRepository);
    }

    @Bean
    public com.imooc.manus.api.domain.repository.TaskExecutionRepository taskExecutionRepository(
            com.imooc.manus.api.infrastructure.repository.jpa.JpaTaskExecutionRepository jpaTaskExecutionRepository) {
        return new JpaTaskExecutionRepositoryImpl(jpaTaskExecutionRepository);
    }

    @Bean
    public com.imooc.manus.api.domain.repository.TaskEventLogRepository taskEventLogRepository(
            com.imooc.manus.api.infrastructure.repository.jpa.JpaTaskEventLogRepository jpaTaskEventLogRepository) {
        return new JpaTaskEventLogRepositoryImpl(jpaTaskEventLogRepository);
    }

    /**
     * UoW工厂（对应Python的 uow_factory）
     */
    @Bean
    public Supplier<IUnitOfWork> uowFactory(
            EntityManager entityManager,
            com.imooc.manus.api.domain.repository.SessionRepository sessionRepository,
            com.imooc.manus.api.domain.repository.FileRepository fileRepository) {
        return () -> new JpaUnitOfWork(entityManager, sessionRepository, fileRepository);
    }

    @Bean
    public AgentMemoryStore agentMemoryStore(Supplier<IUnitOfWork> uowFactory) {
        return new JpaAgentMemoryStore(uowFactory);
    }

    @Bean
    public SessionStateLoader sessionStateLoader(Supplier<IUnitOfWork> uowFactory) {
        return new JpaSessionStateLoader(uowFactory);
    }

    /**
     * 注册重构后的 Agent 执行策略（ReAct = Reasoning + Acting）。
     * 将 Spring AI 的 PlannerReActFlow 适配为统一的 AgentStrategy 接口。
     */
    @Bean
    public com.imooc.manus.api.domain.service.agent.AgentStrategy agentStrategy(
            SpringAIPlannerReActFlow springAIPlannerReActFlow) {
        return new com.imooc.manus.api.domain.service.agent.ReActAgentStrategy(springAIPlannerReActFlow);
    }

    @Bean
    public SandboxPort sandboxPort(@Lazy Sandbox sandbox) {
        return new SandboxPort() {
            @Override
            public com.imooc.manus.common.model.ToolResult<Object> execCommand(String sessionId, String execDir, String command) {
                return toCommonToolResult(sandbox.execCommand(sessionId, execDir, command));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> readShellOutput(String sessionId, boolean console) {
                return toCommonToolResult(sandbox.readShellOutput(sessionId, console));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> waitProcess(String sessionId, Integer seconds) {
                return toCommonToolResult(sandbox.waitProcess(sessionId, seconds));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> writeShellInput(String sessionId, String inputText, boolean pressEnter) {
                return toCommonToolResult(sandbox.writeShellInput(sessionId, inputText, pressEnter));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> killProcess(String sessionId) {
                return toCommonToolResult(sandbox.killProcess(sessionId));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> writeFile(String filepath, String content, boolean append, boolean leadingNewline, boolean trailingNewline, boolean sudo) {
                return toCommonToolResult(sandbox.writeFile(filepath, content, append, leadingNewline, trailingNewline, sudo));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> readFile(String filepath, Integer startLine, Integer endLine, boolean sudo, int maxLength) {
                return toCommonToolResult(sandbox.readFile(filepath, startLine, endLine, sudo, maxLength));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> replaceInFile(String filepath, String oldStr, String newStr, boolean sudo) {
                return toCommonToolResult(sandbox.replaceInFile(filepath, oldStr, newStr, sudo));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> searchInFile(String filepath, String regex, boolean sudo) {
                return toCommonToolResult(sandbox.searchInFile(filepath, regex, sudo));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> findFiles(String dirPath, String globPattern) {
                return toCommonToolResult(sandbox.findFiles(dirPath, globPattern));
            }
        };
    }

    @Bean
    public BrowserPort browserPort(@Lazy Browser browser) {
        return new BrowserPort() {
            @Override
            public com.imooc.manus.common.model.ToolResult<Object> viewPage() {
                return toCommonToolResult(browser.viewPage());
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> navigate(String url) {
                return toCommonToolResult(browser.navigate(url));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> restart(String url) {
                return toCommonToolResult(browser.restart(url));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> click(Integer index, Double coordinateX, Double coordinateY) {
                return toCommonToolResult(browser.click(index, coordinateX, coordinateY));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> input(String text, boolean pressEnter, Integer index, Double coordinateX, Double coordinateY) {
                return toCommonToolResult(browser.input(text, pressEnter, index, coordinateX, coordinateY));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> moveMouse(double coordinateX, double coordinateY) {
                return toCommonToolResult(browser.moveMouse(coordinateX, coordinateY));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> pressKey(String key) {
                return toCommonToolResult(browser.pressKey(key));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> scrollUp(Boolean toTop) {
                return toCommonToolResult(browser.scrollUp(toTop));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> scrollDown(Boolean toBottom) {
                return toCommonToolResult(browser.scrollDown(toBottom));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> consoleExec(String javascript) {
                return toCommonToolResult(browser.consoleExec(javascript));
            }

            @Override
            public com.imooc.manus.common.model.ToolResult<Object> consoleView(Integer maxLines) {
                return toCommonToolResult(browser.consoleView(maxLines));
            }
        };
    }

    @Bean
    public SearchEnginePort searchEnginePort(SearchEngine searchEngine) {
        return new SearchEnginePort() {
            @Override
            public com.imooc.manus.common.model.ToolResult<Object> search(String query, String dateRange) {
                com.imooc.manus.api.domain.model.toolresult.ToolResult<?> result = searchEngine.invoke(query, dateRange);
                return toCommonToolResult(result);
            }
        };
    }

    @Bean
    public SpringAIFlowConfig springAIFlowConfig(AppProperties appProperties) {
        return SpringAIFlowConfig.of(
                appProperties.getLlm().getModelName(),
                appProperties.getAgent().getMaxRetries(),
                appProperties.getAgent().getMaxIterations()
        );
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public List<ToolCallback> springAiToolCallbacks(ToolCallbackRegistry toolCallbackRegistry) {
        return toolCallbackRegistry.buildAll();
    }

    @Bean
    public SpringAIPlannerReActFlow springAIPlannerReActFlow(
            ChatClient chatClient,
            List<ToolCallback> springAiToolCallbacks,
            AgentMemoryStore agentMemoryStore,
            SessionStateLoader sessionStateLoader,
            SpringAIFlowConfig springAIFlowConfig) {
        return new SpringAIPlannerReActFlow(
                chatClient,
                springAiToolCallbacks,
                agentMemoryStore,
                sessionStateLoader,
                springAIFlowConfig
        );
    }

    /**
     * 任务工厂（用于创建RedisStreamTask）
     */
    @Bean
    public RedisTaskFactory redisTaskFactory(
            RedisTemplate<String, String> redisTemplate,
            AppProperties appProperties) {
        return new RedisTaskFactory(redisTemplate, appProperties);
    }

    @Bean
    public TaskDispatchQueue taskDispatchQueue(
            RedisTemplate<String, String> redisTemplate,
            AppProperties appProperties) {
        return new RedisTaskDispatchQueue(
                redisTemplate,
                appProperties.getAgent().getDispatchStreamKey(),
                appProperties.getAgent().getDispatchConsumerGroup()
        );
    }

    private static com.imooc.manus.common.model.ToolResult<Object> toCommonToolResult(
            com.imooc.manus.api.domain.model.toolresult.ToolResult<?> result) {
        if (result == null) {
            return com.imooc.manus.common.model.ToolResult.error("tool result is null");
        }
        return result.success()
                ? com.imooc.manus.common.model.ToolResult.success(result.data(), result.message())
                : com.imooc.manus.common.model.ToolResult.error(result.message());
    }
}

