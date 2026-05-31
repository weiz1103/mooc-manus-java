package io.github.weiz1103.agentflow.api.infrastructure.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.weiz1103.agentflow.api.domain.model.appconfig.AppConfig;
import io.github.weiz1103.agentflow.api.domain.repository.AppConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

/**
 * 基于YAML文件的应用配置仓库。
 * <p>
 * 读写 config.yaml 文件。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-29 17:50:07
 */
public class FileAppConfigRepository implements AppConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileAppConfigRepository.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private final String configPath;
    private final Path resolvedConfigPath;

    public FileAppConfigRepository(String configPath) {
        this.configPath = configPath;
        this.resolvedConfigPath = resolveConfigPath(configPath);
    }

    /**
     * 加载获取应用配置
     *
     * @return 应用配置（Optional。
     */
    @Override
    @SuppressWarnings("unchecked")
    public Optional<AppConfig> load() {
        Path path = resolvedConfigPath;
        if (!Files.exists(path)) {
            logger.warn("配置文件不存。 {}", path);
            return Optional.of(new AppConfig());
        }

        try (InputStream is = Files.newInputStream(path)) {
            Yaml yaml = new Yaml();
            Map<String, Object> rawConfig = yaml.load(is);
            if (rawConfig == null) return Optional.of(new AppConfig());

            // 将YAML数据转换为AppConfig
            String json = OBJECT_MAPPER.writeValueAsString(rawConfig);
            AppConfig appConfig = OBJECT_MAPPER.readValue(json, AppConfig.class);
            return Optional.of(appConfig);
        } catch (Exception e) {
            logger.error("加载配置文件失败: {}", e.getMessage());
            return Optional.of(new AppConfig());
        }
    }

    /**
     * 存储更新的应用配。
     *
     * @param appConfig 应用配置
     */
    @Override
    public void save(AppConfig appConfig) {
        try {
            // 1.将AppConfig转换为Map
            @SuppressWarnings("unchecked")
            Map<String, Object> configMap = OBJECT_MAPPER.convertValue(appConfig, Map.class);

            // 2.将Map写入YAML文件
            Yaml yaml = new Yaml();
            Path path = resolvedConfigPath;
            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path)) {
                yaml.dump(configMap, writer);
            }

            logger.info("应用配置保存成功: {}", configPath);
        } catch (Exception e) {
            logger.error("保存配置文件失败: {}", e.getMessage());
            throw new RuntimeException("保存配置文件失败: " + e.getMessage(), e);
        }
    }

    private Path resolveConfigPath(String configuredPath) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            Path configured = Paths.get(configuredPath).normalize();
            if (Files.exists(configured)) {
                logger.info("使用显式配置文件路径: {}", configured);
                return configured;
            }
        }

        for (String candidate : new String[]{"../api/config.yaml", "api/config.yaml", "./config.yaml"}) {
            Path path = Paths.get(candidate).normalize();
            if (Files.exists(path)) {
                logger.info("自动发现配置文件路径: {}", path);
                return path;
            }
        }

        Path fallback = Paths.get(configuredPath == null || configuredPath.isBlank() ? "../api/config.yaml" : configuredPath)
                .normalize();
        logger.warn("未找到现有配置文件，回退到路。 {}", fallback);
        return fallback;
    }
}


