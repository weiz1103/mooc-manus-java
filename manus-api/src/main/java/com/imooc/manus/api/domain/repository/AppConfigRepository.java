package com.imooc.manus.api.domain.repository;

import com.imooc.manus.api.domain.model.appconfig.AppConfig;

import java.util.Optional;

/**
 * 应用配置仓库。
 * 对应Python中的 AppConfigRepository Protocol。
 *
 * @author thezehui@gmail.com
 */
public interface AppConfigRepository {

    /**
     * 加载获取应用配置
     *
     * @return 应用配置（Optional）
     */
    Optional<AppConfig> load();

    /**
     * 存储更新的应用配置
     *
     * @param appConfig 应用配置
     */
    void save(AppConfig appConfig);
}

