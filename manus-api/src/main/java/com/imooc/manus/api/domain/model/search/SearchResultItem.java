package com.imooc.manus.api.domain.model.search;

/**
 * 搜索结果条目数据模型。
 * <p>
 * </p>
 *
 * @param url     搜索条目URL地址
 * @param title   搜索条目标题
 * @param snippet 搜索条目简介
 * @author zhuang03@qq.com
 * @date 2026-05-27 19:51:40
 */
public record SearchResultItem(
        String url,
        String title,
        String snippet
) {
    public SearchResultItem(String url, String title) {
        this(url, title, "");
    }
}

