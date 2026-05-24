package com.imooc.manus.api.domain.model.search;

/**
 * 搜索结果条目数据模型。
 * <p>
 * 对应Python中的 SearchResultItem Pydantic BaseModel。
 * </p>
 *
 * @param url     搜索条目URL地址
 * @param title   搜索条目标题
 * @param snippet 搜索条目简介
 * @author thezehui@gmail.com
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

