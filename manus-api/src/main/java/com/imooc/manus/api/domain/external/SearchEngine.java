package com.imooc.manus.api.domain.external;

import com.imooc.manus.api.domain.model.search.SearchResults;
import com.imooc.manus.api.domain.model.toolresult.ToolResult;

/**
 * 搜索引擎API接口协议。
 * @author zhuang03@qq.com
 * @date 2026-05-30 05:05:36
 */
public interface SearchEngine {

    /**
     * 调用搜索引擎并传递query+date_range调用搜索引擎获取数据
     *
     * @param query     搜索关键词
     * @param dateRange 日期检索范围（可为null，如 "past_week"）
     * @return 搜索结果
     */
    ToolResult<SearchResults> invoke(String query, String dateRange);
}

