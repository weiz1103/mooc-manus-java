package io.github.weiz1103.agentflow.api.domain.external;

import io.github.weiz1103.agentflow.api.domain.model.search.SearchResults;
import io.github.weiz1103.agentflow.api.domain.model.toolresult.ToolResult;

/**
 * 搜索引擎API接口协议。
 * @author zhuang03@qq.com
 * @date 2026-05-30 05:05:36
 */
public interface SearchEngine {

    /**
     * 调用搜索引擎并传递query+date_range调用搜索引擎获取数据
     *
     * @param query     搜索关键。
     * @param dateRange 日期检索范围（可为null，如 "past_week"。
     * @return 搜索结果
     */
    ToolResult<SearchResults> invoke(String query, String dateRange);
}


