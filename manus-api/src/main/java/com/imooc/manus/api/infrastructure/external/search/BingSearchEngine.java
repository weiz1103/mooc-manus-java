package com.imooc.manus.api.infrastructure.external.search;

import com.imooc.manus.api.domain.external.SearchEngine;
import com.imooc.manus.api.domain.model.search.SearchResultItem;
import com.imooc.manus.api.domain.model.search.SearchResults;
import com.imooc.manus.api.domain.model.toolresult.ToolResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bing搜索引擎。
 * <p>
 * 使用Java HttpClient + Jsoup实现Bing网页搜索（类似Python的httpx + bs4）。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-29 17:40:17
 */
public class BingSearchEngine implements SearchEngine {

    private static final Logger logger = LoggerFactory.getLogger(BingSearchEngine.class);
    private static final String BASE_URL = "https://www.bing.com/search";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0";

    private final HttpClient httpClient;

    public BingSearchEngine() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 传递query+date_range使用httpClient+jsoup调用bing搜索并获取搜索结果。
     *
     * @param query     搜索关键词
     * @param dateRange 日期检索范围（可为null，如 "past_week"）
     * @return 搜索结果
     */
    @Override
    public ToolResult<SearchResults> invoke(String query, String dateRange) {
        try {
            // 1.构建请求URL
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL)
                    .queryParam("q", query);

            // 2.判断date_range是否存在并提取真实检索数据
            if (dateRange != null && !"all".equals(dateRange)) {
                // 3.获取当前日期的天数距离1970-01-01的天数
                long daysSinceEpoch = Instant.now().getEpochSecond() / (24 * 60 * 60);

                // 4.创建日期检索数据类型映射
                Map<String, String> dateMapping = Map.of(
                        "past_hour", "ex1%3a\"ez1\"",
                        "past_day", "ex1%3a\"ez1\"",
                        "past_week", "ex1%3a\"ez2\"",
                        "past_month", "ex1%3a\"ez3\"",
                        "past_year", "ex1%3a\"ez5_" + (daysSinceEpoch - 365) + "_" + daysSinceEpoch + "\""
                );

                // 5.判断是否传递了date_range补全params参数
                String filterValue = dateMapping.get(dateRange);
                if (filterValue != null) {
                    uriBuilder.queryParam("filters", filterValue);
                }
            }

            String url = uriBuilder.build().toUri().toString();

            // 6.构建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Upgrade-Insecure-Requests", "1")
                    .GET()
                    .build();

            // 7.发起请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 8.使用jsoup解析html内容
            Document soup = Jsoup.parse(response.body());

            // 9.定义搜索结果并解析li.b_algo对应的dom元素
            List<SearchResultItem> searchResults = new ArrayList<>();
            Elements resultItems = soup.select("li.b_algo");

            // 10.循环遍历所有匹配的dom
            for (Element item : resultItems) {
                try {
                    // 11.定义变量存储数据
                    String title = "";
                    String itemUrl = "";

                    // 12.解析搜索结果中的标题与URL链接
                    Element titleTag = item.selectFirst("h2");
                    if (titleTag != null) {
                        Element aTag = titleTag.selectFirst("a");
                        if (aTag != null) {
                            title = aTag.text().trim();
                            itemUrl = aTag.attr("href");
                        }
                    }

                    // 13.判断标题如果不存在提取该dom下的a标签
                    if (title.isEmpty()) {
                        Elements aTags = item.select("a");
                        if (!aTags.isEmpty()) {
                            Element firstA = aTags.get(0);
                            title = firstA.text().trim();
                            itemUrl = firstA.attr("href");
                        }
                    }

                    // 14.提取摘要内容
                    String snippet = "";
                    Element snippetEl = item.selectFirst(".b_caption p");
                    if (snippetEl != null) {
                        snippet = snippetEl.text().trim();
                    }

                    // 15.过滤掉没有有效URL的结果
                    if (!itemUrl.isEmpty() && itemUrl.startsWith("http")) {
                        searchResults.add(new SearchResultItem(itemUrl, title, snippet));
                    }
                } catch (Exception e) {
                    logger.warn("解析Bing搜索结果条目时出错: {}", e.getMessage());
                }
            }

            SearchResults results = new SearchResults(query, dateRange, searchResults.size(), searchResults);
            return ToolResult.ok(results);
        } catch (Exception e) {
            logger.error("调用Bing搜索引擎出错: {}", e.getMessage(), e);
            return ToolResult.fail("调用Bing搜索引擎出错: " + e.getMessage());
        }
    }
}

