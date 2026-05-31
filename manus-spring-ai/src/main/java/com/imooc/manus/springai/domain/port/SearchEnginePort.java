package com.imooc.manus.springai.domain.port;

import com.imooc.manus.common.model.ToolResult;

/**
 * 搜索引擎服务端口（Port / Anti-Corruption Layer）。
 * @author zhuang03@qq.com
 * @date 2026-05-30 08:25:01
 */
public interface SearchEnginePort {

    /**
     * 执行全网搜索。
     *
     * @param query     搜索查询内容
     * @param dateRange 日期范围限制（可为 null）
     * @return 搜索结果
     */
    ToolResult<Object> search(String query, String dateRange);
}

