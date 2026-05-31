package com.imooc.manus.springai.domain.port;

import com.imooc.manus.common.model.ToolResult;

/**
 * 浏览器服务端口（Port / Anti-Corruption Layer）。
 * @author zhuang03@qq.com
 * @date 2026-05-28 20:16:17
 */
public interface BrowserPort {

    ToolResult<Object> viewPage();

    ToolResult<Object> navigate(String url);

    ToolResult<Object> restart(String url);

    ToolResult<Object> click(Integer index, Double coordinateX, Double coordinateY);

    ToolResult<Object> input(String text, boolean pressEnter, Integer index,
                             Double coordinateX, Double coordinateY);

    ToolResult<Object> moveMouse(double coordinateX, double coordinateY);

    ToolResult<Object> pressKey(String key);

    ToolResult<Object> scrollUp(Boolean toTop);

    ToolResult<Object> scrollDown(Boolean toBottom);

    ToolResult<Object> consoleExec(String javascript);

    ToolResult<Object> consoleView(Integer maxLines);
}

