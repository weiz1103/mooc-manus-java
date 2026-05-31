package io.github.weiz1103.agentflow.api.domain.model.search;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果数据模型。
 * <p>
 * </p>
 *
 * @param query        用户的搜索词
 * @param dateRange    日期检索范围（可为null。
 * @param totalResults 搜索结果总条。
 * @param results      搜索结果列表
 * @author zhuang03@qq.com
 * @date 2026-05-25 23:44:39
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


