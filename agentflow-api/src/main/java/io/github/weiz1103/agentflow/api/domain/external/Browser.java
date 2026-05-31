package io.github.weiz1103.agentflow.api.domain.external;

import io.github.weiz1103.agentflow.api.domain.model.toolresult.ToolResult;

/**
 * 浏览器服务扩展，涵盖：访问页面、URL跳转、输入、移动鼠标、滚动、截图、执行js代码等。
 * <p>
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-31 10:37:29
 */
public interface Browser {

    /**
     * 浏览获取当前浏览器的页面内容
     *
     * @return 工具结果
     */
    ToolResult<Object> viewPage();

    /**
     * 传递对应的url使用浏览器导航到该页。
     *
     * @param url 要访问的URL
     * @return 工具结果
     */
    ToolResult<Object> navigate(String url);

    /**
     * 重启浏览器并访问对应的URL
     *
     * @param url 要访问的URL
     * @return 工具结果
     */
    ToolResult<Object> restart(String url);

    /**
     * 传递对应的元素索引或者页面y坐标实现点击功能
     *
     * @param index       元素索引（可为null。
     * @param coordinateX x坐标（可为null。
     * @param coordinateY y坐标（可为null。
     * @return 工具结果
     */
    ToolResult<Object> click(Integer index, Double coordinateX, Double coordinateY);

    /**
     * 传递文。回车标记+索引+xy坐标实现在网页输入框中输入对应内。
     *
     * @param text        文本内容
     * @param pressEnter  输入后是否按下回车键
     * @param index       元素索引（可为null。
     * @param coordinateX x坐标（可为null。
     * @param coordinateY y坐标（可为null。
     * @return 工具结果
     */
    ToolResult<Object> input(String text, boolean pressEnter, Integer index,
                             Double coordinateX, Double coordinateY);

    /**
     * 传递对应的xy坐标移动鼠标
     *
     * @param coordinateX x坐标
     * @param coordinateY y坐标
     * @return 工具结果
     */
    ToolResult<Object> moveMouse(double coordinateX, double coordinateY);

    /**
     * 传递按键标识实现浏览器模拟按键
     *
     * @param key 按键名称（如Enter、Tab、ArrowUp。
     * @return 工具结果
     */
    ToolResult<Object> pressKey(String key);

    /**
     * 传递索。下拉元素选项在下拉菜单中选择指定的选项
     *
     * @param index  元素索引
     * @param option 选项序号
     * @return 工具结果
     */
    ToolResult<Object> selectOption(int index, int option);

    /**
     * 向上滚动浏览器，如果没传递to_top=True则向上滚动一。
     *
     * @param toTop 是否直接滚动到页面顶部（可为null。
     * @return 工具结果
     */
    ToolResult<Object> scrollUp(Boolean toTop);

    /**
     * 向下滚动浏览器，如果没传递to_down=True则向下滚动一。
     *
     * @param toDown 是否直接滚动到页面底部（可为null。
     * @return 工具结果
     */
    ToolResult<Object> scrollDown(Boolean toDown);

    /**
     * 对当前浏览器的页面进行截图，传递full_page=True则意味着全页截图
     *
     * @param fullPage 是否全页截图（可为null。
     * @return 截图字节数组
     */
    byte[] screenshot(Boolean fullPage);

    /**
     * 传递对应的js脚本在浏览器的控制台执行
     *
     * @param javascript js脚本
     * @return 工具结果
     */
    ToolResult<Object> consoleExec(String javascript);

    /**
     * 传递最大输出行数，获取控制台的输出结果，如果不传递则获取所有结。
     *
     * @param maxLines 最大行数（可为null。
     * @return 工具结果
     */
    ToolResult<Object> consoleView(Integer maxLines);
}


