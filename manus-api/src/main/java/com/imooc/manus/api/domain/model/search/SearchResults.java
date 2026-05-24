package com.imooc.manus.api.domain.model.search;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果数据模型。
 * <p>
 * 对应Python中的 SearchResults Pydantic BaseModel。
 * </p>
 *
 * @param query        用户的搜索词
 * @param dateRange    日期检索范围（可为null）
 * @param totalResults 搜索结果总条数
 * @param results      搜索结果列表
 * @author thezehui@gmail.com
 */
public record SearchResults(
        String query,
        String dateRange,
        int totalResults,
        List<SearchResultItem> results
) {
    public SearchResults(String query) {
        this(query, null, 0, new ArrayList<>());
    }
}

