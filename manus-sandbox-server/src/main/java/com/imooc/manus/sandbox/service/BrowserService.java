package com.imooc.manus.sandbox.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.imooc.manus.sandbox.constant.BrowserJsConstants;
import com.imooc.manus.common.dto.browser.BrowserDto;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 浏览器自动化服务，对应 Python PlaywrightBrowser。
 *
 * <p>关键设计：
 * <ul>
 *   <li>每个 sessionId 独立拥有一个 Page（避免并发干扰）</li>
 *   <li>Playwright 实例在首次使用时懒加载（节省内存）</li>
 *   <li>使用 FlexmarkHtmlConverter 将页面 HTML 转为 Markdown（对应 Python markdownify）</li>
 * </ul>
 */
@Service
@Slf4j
public class BrowserService {

    private Playwright playwright;
    private Browser browser;

    /** 每个会话对应独立的 Page，对应 Python 中的 browser.new_page() */
    private final Map<String, Page> pageMap = new ConcurrentHashMap<>();

    private final FlexmarkHtmlConverter htmlConverter = FlexmarkHtmlConverter.builder().build();

    // ==================== Lazy Init ====================

    private synchronized Browser getBrowser() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(List.of(
                                    "--no-sandbox",
                                    "--disable-setuid-sandbox",
                                    "--disable-dev-shm-usage"
                            ))
            );
            log.info("Playwright browser initialized");
        }
        return browser;
    }

    private Page getOrCreatePage(String sessionId) {
        return pageMap.computeIfAbsent(sessionId, id -> {
            BrowserContext ctx = getBrowser().newContext(
                    new Browser.NewContextOptions()
                            .setViewportSize(1280, 800)
                            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                                    + "AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
            );
            Page p = ctx.newPage();
            log.info("Created new browser page for session: {}", id);
            return p;
        });
    }

    // ==================== Public API ====================

    /**
     * 导航到指定 URL，对应 Python PlaywrightBrowser.navigate()
     */
    public BrowserDto.NavigateResult navigate(String sessionId, String url) {
        log.info("[{}] Navigating to {}", sessionId, url);
        Page page = getOrCreatePage(sessionId);
        try {
            page.navigate(url);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            return BrowserDto.NavigateResult.builder()
                    .url(page.url())
                    .title(page.title())
                    .status("success")
                    .build();
        } catch (Exception e) {
            log.error("[{}] Navigation failed: {}", sessionId, e.getMessage());
            return BrowserDto.NavigateResult.builder()
                    .url(url).status("error").title(e.getMessage()).build();
        }
    }

    /**
     * 获取当前页面内容和可交互元素，对应 Python PlaywrightBrowser 中的 view 方法
     */
    public BrowserDto.ViewPageResult viewPage(String sessionId) {
        Page page = getOrCreatePage(sessionId);

        // 注入元素标记脚本（与 Python 版完全相同的 JS 逻辑）
        page.evaluate(BrowserJsConstants.GET_INTERACTIVE_ELEMENTS_FUNC);

        // 获取可见文本内容并转换为 Markdown
        String htmlContent = (String) page.evaluate(BrowserJsConstants.GET_VISIBLE_CONTENT_FUNC);
        String markdownContent = htmlConverter.convert(htmlContent != null ? htmlContent : "");

        // 获取标记的可交互元素列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elements = (List<Map<String, Object>>)
                page.evaluate("() => window.__manusInteractiveElements || []");

        // 截图（Base64 编码）
        byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
        String screenshot = Base64.getEncoder().encodeToString(screenshotBytes);

        return BrowserDto.ViewPageResult.builder()
                .url(page.url())
                .content(markdownContent)
                .interactiveElements(elements != null ? elements : new ArrayList<>())
                .screenshot(screenshot)
                .build();
    }

    /**
     * 点击元素，对应 Python PlaywrightBrowser.click()
     */
    public BrowserDto.ActionResult click(String sessionId, int index) {
        log.info("[{}] Clicking element index={}", sessionId, index);
        Page page = getOrCreatePage(sessionId);
        try {
            String selector = String.format("[data-manus-id=\"manus-element-%d\"]", index);
            page.click(selector);
            return BrowserDto.ActionResult.builder().success(true).message("Click successful").build();
        } catch (Exception e) {
            return BrowserDto.ActionResult.builder().success(false).message(e.getMessage()).build();
        }
    }

    /**
     * 向元素输入文字，对应 Python PlaywrightBrowser.type()
     */
    public BrowserDto.ActionResult type(String sessionId, int index, String text, boolean pressEnter) {
        log.info("[{}] Typing to element index={}", sessionId, index);
        Page page = getOrCreatePage(sessionId);
        try {
            String selector = String.format("[data-manus-id=\"manus-element-%d\"]", index);
            page.fill(selector, "");  // 先清空
            page.type(selector, text);
            if (pressEnter) {
                page.keyboard().press("Enter");
            }
            return BrowserDto.ActionResult.builder().success(true).message("Type successful").build();
        } catch (Exception e) {
            return BrowserDto.ActionResult.builder().success(false).message(e.getMessage()).build();
        }
    }

    /**
     * 关闭指定会话的浏览器页面
     */
    public void closePage(String sessionId) {
        Page page = pageMap.remove(sessionId);
        if (page != null && !page.isClosed()) {
            try {
                page.context().close();
                log.info("Closed browser page for session: {}", sessionId);
            } catch (Exception e) {
                log.warn("Error closing page for session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Cleaning up Playwright resources");
        pageMap.forEach((id, page) -> {
            try { page.context().close(); } catch (Exception ignored) {}
        });
        pageMap.clear();
        if (browser != null) {
            try { browser.close(); } catch (Exception ignored) {}
        }
        if (playwright != null) {
            try { playwright.close(); } catch (Exception ignored) {}
        }
    }
}
