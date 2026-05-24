package com.imooc.manus.sandbox.controller;

import com.imooc.manus.common.dto.ApiResponse;
import com.imooc.manus.common.dto.browser.BrowserDto;
import com.imooc.manus.sandbox.service.BrowserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 浏览器控制器，对应 Python sandbox_routes 中的 browser 相关端点。
 */
@RestController
@RequestMapping("/api/browser")
@RequiredArgsConstructor
@Slf4j
public class BrowserController {

    private final BrowserService browserService;

    @PostMapping("/navigate")
    public ApiResponse<BrowserDto.NavigateResult> navigate(@RequestBody BrowserDto.NavigateRequest request) {
        BrowserDto.NavigateResult result = browserService.navigate(request.getSessionId(), request.getUrl());
        return ApiResponse.success(result, "导航成功");
    }

    @GetMapping("/{sessionId}/view")
    public ApiResponse<BrowserDto.ViewPageResult> viewPage(@PathVariable String sessionId) {
        return ApiResponse.success(browserService.viewPage(sessionId), "获取页面内容成功");
    }

    @PostMapping("/click")
    public ApiResponse<BrowserDto.ActionResult> click(@RequestBody BrowserDto.ClickRequest request) {
        return ApiResponse.success(browserService.click(request.getSessionId(), request.getIndex()));
    }

    @PostMapping("/type")
    public ApiResponse<BrowserDto.ActionResult> type(@RequestBody BrowserDto.TypeRequest request) {
        return ApiResponse.success(browserService.type(
                request.getSessionId(), request.getIndex(),
                request.getText(), request.isPressEnter()));
    }

    @PostMapping("/{sessionId}/close")
    public ApiResponse<Void> closePage(@PathVariable String sessionId) {
        browserService.closePage(sessionId);
        return ApiResponse.success("浏览器页面已关闭");
    }
}
