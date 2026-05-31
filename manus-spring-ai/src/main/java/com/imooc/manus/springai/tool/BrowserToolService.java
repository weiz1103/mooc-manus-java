package com.imooc.manus.springai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.springai.domain.port.BrowserPort;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 浏览器工具服务，提供页面导航、点击、输入、滚动、JS 执行等能力。
 * <p>
 * </p>
 *
 * <p><b>扩展点：</b> 注入不同的 {@link BrowserPort} 实现（如 Selenium、Playwright）即可切换底层驱动。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-30 02:34:13
 */
public class BrowserToolService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final BrowserPort browser;

    public BrowserToolService(BrowserPort browser) {
        this.browser = browser;
    }

    @Tool(name = "browser_view", description = "查看当前浏览器页面内容，用于确认已打开页面的最新状态。")
    public String browserView() {
        return serialize(browser.viewPage());
    }

    @Tool(name = "browser_navigate", description = "将浏览器导航至指定网址，当需要访问新页面时使用。")
    public String browserNavigate(
            @ToolParam(description = "要访问的完整 URL") String url
    ) {
        return serialize(browser.navigate(url));
    }

    @Tool(name = "browser_restart", description = "重新启动浏览器并导航至指定URL，当需要重置浏览器时使用。")
    public String browserRestart(
            @ToolParam(description = "重启后要访问的 URL") String url
    ) {
        return serialize(browser.restart(url));
    }

    @Tool(name = "browser_click", description = "点击当前页面中的元素，在需要点击页面元素时使用。")
    public String browserClick(
            @ToolParam(description = "元素索引（与坐标二选一）", required = false) Integer index,
            @ToolParam(description = "点击 X 坐标（与索引二选一）", required = false) Double coordinateX,
            @ToolParam(description = "点击 Y 坐标（与索引二选一）", required = false) Double coordinateY
    ) {
        return serialize(browser.click(index, coordinateX, coordinateY));
    }

    @Tool(name = "browser_input", description = "覆盖浏览器当前页面可编辑区域的文本（input/textarea 输入框），在需要填充输入框时使用。")
    public String browserInput(
            @ToolParam(description = "要输入的文本内容") String text,
            @ToolParam(description = "输入后是否按 Enter 键") Boolean pressEnter,
            @ToolParam(description = "元素索引（可选）", required = false) Integer index,
            @ToolParam(description = "输入框 X 坐标（可选）", required = false) Double coordinateX,
            @ToolParam(description = "输入框 Y 坐标（可选）", required = false) Double coordinateY
    ) {
        return serialize(browser.input(text, Boolean.TRUE.equals(pressEnter), index, coordinateX, coordinateY));
    }

    @Tool(name = "browser_move_mouse", description = "将鼠标光标移动至当前浏览器页面的指定位置，用于模拟用户的鼠标移动。")
    public String browserMoveMouse(
            @ToolParam(description = "目标 X 坐标") double coordinateX,
            @ToolParam(description = "目标 Y 坐标") double coordinateY
    ) {
        return serialize(browser.moveMouse(coordinateX, coordinateY));
    }

    @Tool(name = "browser_press_key", description = "在当前浏览器页面模拟按键，当需要执行特定的键盘操作时使用。")
    public String browserPressKey(
            @ToolParam(description = "按键名称，如 Enter、Tab、ArrowUp") String key
    ) {
        return serialize(browser.pressKey(key));
    }

    @Tool(name = "browser_scroll_up", description = "向上滚动浏览器页面，用于查看上方内容或返回页面顶部。")
    public String browserScrollUp(
            @ToolParam(description = "是否直接滚动到页面顶部", required = false) Boolean toTop
    ) {
        return serialize(browser.scrollUp(toTop));
    }

    @Tool(name = "browser_scroll_down", description = "向下滚动当前浏览器页面，用于查看下方内容或跳转到页面底部。")
    public String browserScrollDown(
            @ToolParam(description = "是否直接滚动到页面底部", required = false) Boolean toBottom
    ) {
        return serialize(browser.scrollDown(toBottom));
    }

    @Tool(name = "browser_console_exec", description = "在浏览器控制台中执行 JavaScript 代码，当需要执行自定义脚本时使用。")
    public String browserConsoleExec(
            @ToolParam(description = "要执行的 JavaScript 代码") String javascript
    ) {
        return serialize(browser.consoleExec(javascript));
    }

    @Tool(name = "browser_console_view", description = "查看浏览器控制台输出，用于检查 JavaScript 日志或调试页面错误。")
    public String browserConsoleView(
            @ToolParam(description = "最多返回的日志行数", required = false) Integer maxLines
    ) {
        return serialize(browser.consoleView(maxLines));
    }

    private String serialize(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"序列化失败\"}";
        }
    }
}

