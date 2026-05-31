package com.imooc.manus.api.domain.repository;

import com.imooc.manus.api.domain.model.appconfig.AppConfig;

import java.util.Optional;

/**
 * 应用配置仓库。
 * @author zhuang03@qq.com
 * @date 2026-05-28 19:24:00
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

