package io.github.weiz1103.agentflow.api.infrastructure.config;

import io.github.weiz1103.agentflow.api.domain.external.*;
import io.github.weiz1103.agentflow.api.domain.model.appconfig.A2AConfig;
import io.github.weiz1103.agentflow.api.domain.model.appconfig.AgentConfig;
import io.github.weiz1103.agentflow.api.domain.model.appconfig.AppConfig;
import io.github.weiz1103.agentflow.api.domain.model.appconfig.LLMConfig;
import io.github.weiz1103.agentflow.api.domain.model.appconfig.MCPConfig;
import io.github.weiz1103.agentflow.api.domain.repository.IUnitOfWork;
import io.github.weiz1103.agentflow.api.infrastructure.external.jsonparser.RepairJsonParser;
import io.github.weiz1103.agentflow.api.infrastructure.external.llm.OpenAILLM;
import io.github.weiz1103.agentflow.api.infrastructure.external.search.BingSearchEngine;
import io.github.weiz1103.agentflow.api.infrastructure.external.sandbox.DockerSandbox;
import io.github.weiz1103.agentflow.api.infrastructure.external.task.RedisTaskDispatchQueue;
import io.github.weiz1103.agentflow.api.infrastructure.repository.*;
import io.github.weiz1103.agentflow.api.infrastructure.repository.jpa.JpaSessionRepository;
import io.github.weiz1103.agentflow.api.infrastructure.springai.JpaAgentMemoryStore;
import io.github.weiz1103.agentflow.api.infrastructure.springai.JpaSessionStateLoader;
import io.github.weiz1103.agentflow.springai.config.SpringAIFlowConfig;
import io.github.weiz1103.agentflow.springai.domain.port.BrowserPort;
import io.github.weiz1103.agentflow.springai.domain.port.SandboxPort;
import io.github.weiz1103.agentflow.springai.domain.port.SearchEnginePort;
import io.github.weiz1103.agentflow.springai.flow.SpringAIPlannerReActFlow;
import io.github.weiz1103.agentflow.springai.memory.AgentMemoryStore;
import io.github.weiz1103.agentflow.springai.session.SessionStateLoader;
import io.github.weiz1103.agentflow.springai.tool.registry.ToolCallbackRegistry;
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
 * 遵循构造器注入原则，不使用@Autowired字段注入。
 * </p>
 *

 */
@Configuration
public class AgentFlowConfig {

    /**
     * 。application.yml 组装。AppConfig。
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
     * JSON解析。
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
    public io.github.weiz1103.agentflow.api.domain.repository.FileRepository fileRepository() {
        return new InMemoryFileRepository();
    }

    /**
     * 会话仓库
     */
    @Bean
    public io.github.weiz1103.agentflow.api.domain.repository.SessionRepository sessionRepository(
            JpaSessionRepository jpaSessionRepository) {
        return new JpaSessionRepositoryImpl(jpaSessionRepository);
    }

    @Bean
    public io.github.weiz1103.agentflow.api.domain.repository.TaskExecutionRepository taskExecutionRepository(
            io.github.weiz1103.agentflow.api.infrastructure.repository.jpa.JpaTaskExecutionRepository jpaTaskExecutionRepository) {
        return new JpaTaskExecutionRepositoryImpl(jpaTaskExecutionRepository);
    }

    @Bean
    public io.github.weiz1103.agentflow.api.domain.repository.TaskEventLogRepository taskEventLogRepository(
            io.github.weiz1103.agentflow.api.infrastructure.repository.jpa.JpaTaskEventLogRepository jpaTaskEventLogRepository) {
        return new JpaTaskEventLogRepositoryImpl(jpaTaskEventLogRepository);
    }

    @Bean
    public Supplier<IUnitOfWork> uowFactory(
            EntityManager entityManager,
            io.github.weiz1103.agentflow.api.domain.repository.SessionRepository sessionRepository,
            io.github.weiz1103.agentflow.api.domain.repository.FileRepository fileRepository) {
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
     * 。Spring AI 。PlannerReActFlow 适配为统一。AgentStrategy 接口。
     */
    @Bean
    public io.github.weiz1103.agentflow.api.domain.service.agent.AgentStrategy agentStrategy(
            SpringAIPlannerReActFlow springAIPlannerReActFlow) {
        return new io.github.weiz1103.agentflow.api.domain.service.agent.ReActAgentStrategy(springAIPlannerReActFlow);
    }

    @Bean
    public SandboxPort sandboxPort(@Lazy Sandbox sandbox) {
        return new SandboxPort() {
            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> execCommand(String sessionId, String execDir, String command) {
                return toCommonToolResult(sandbox.execCommand(sessionId, execDir, command));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> readShellOutput(String sessionId, boolean console) {
                return toCommonToolResult(sandbox.readShellOutput(sessionId, console));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> waitProcess(String sessionId, Integer seconds) {
                return toCommonToolResult(sandbox.waitProcess(sessionId, seconds));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> writeShellInput(String sessionId, String inputText, boolean pressEnter) {
                return toCommonToolResult(sandbox.writeShellInput(sessionId, inputText, pressEnter));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> killProcess(String sessionId) {
                return toCommonToolResult(sandbox.killProcess(sessionId));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> writeFile(String filepath, String content, boolean append, boolean leadingNewline, boolean trailingNewline, boolean sudo) {
                return toCommonToolResult(sandbox.writeFile(filepath, content, append, leadingNewline, trailingNewline, sudo));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> readFile(String filepath, Integer startLine, Integer endLine, boolean sudo, int maxLength) {
                return toCommonToolResult(sandbox.readFile(filepath, startLine, endLine, sudo, maxLength));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> replaceInFile(String filepath, String oldStr, String newStr, boolean sudo) {
                return toCommonToolResult(sandbox.replaceInFile(filepath, oldStr, newStr, sudo));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> searchInFile(String filepath, String regex, boolean sudo) {
                return toCommonToolResult(sandbox.searchInFile(filepath, regex, sudo));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> findFiles(String dirPath, String globPattern) {
                return toCommonToolResult(sandbox.findFiles(dirPath, globPattern));
            }
        };
    }

    @Bean
    public BrowserPort browserPort(@Lazy Browser browser) {
        return new BrowserPort() {
            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> viewPage() {
                return toCommonToolResult(browser.viewPage());
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> navigate(String url) {
                return toCommonToolResult(browser.navigate(url));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> restart(String url) {
                return toCommonToolResult(browser.restart(url));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> click(Integer index, Double coordinateX, Double coordinateY) {
                return toCommonToolResult(browser.click(index, coordinateX, coordinateY));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> input(String text, boolean pressEnter, Integer index, Double coordinateX, Double coordinateY) {
                return toCommonToolResult(browser.input(text, pressEnter, index, coordinateX, coordinateY));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> moveMouse(double coordinateX, double coordinateY) {
                return toCommonToolResult(browser.moveMouse(coordinateX, coordinateY));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> pressKey(String key) {
                return toCommonToolResult(browser.pressKey(key));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> scrollUp(Boolean toTop) {
                return toCommonToolResult(browser.scrollUp(toTop));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> scrollDown(Boolean toBottom) {
                return toCommonToolResult(browser.scrollDown(toBottom));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> consoleExec(String javascript) {
                return toCommonToolResult(browser.consoleExec(javascript));
            }

            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> consoleView(Integer maxLines) {
                return toCommonToolResult(browser.consoleView(maxLines));
            }
        };
    }

    @Bean
    public SearchEnginePort searchEnginePort(SearchEngine searchEngine) {
        return new SearchEnginePort() {
            @Override
            public io.github.weiz1103.agentflow.common.model.ToolResult<Object> search(String query, String dateRange) {
                io.github.weiz1103.agentflow.api.domain.model.toolresult.ToolResult<?> result = searchEngine.invoke(query, dateRange);
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
     * 任务工厂（用于创建RedisStreamTask。
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

    private static io.github.weiz1103.agentflow.common.model.ToolResult<Object> toCommonToolResult(
            io.github.weiz1103.agentflow.api.domain.model.toolresult.ToolResult<?> result) {
        if (result == null) {
            return io.github.weiz1103.agentflow.common.model.ToolResult.error("tool result is null");
        }
        return result.success()
                ? io.github.weiz1103.agentflow.common.model.ToolResult.success(result.data(), result.message())
                : io.github.weiz1103.agentflow.common.model.ToolResult.error(result.message());
    }
}



