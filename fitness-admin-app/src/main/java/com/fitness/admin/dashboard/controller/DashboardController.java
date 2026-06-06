package com.fitness.admin.dashboard.controller;

import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.dashboard.service.DashboardService;
import com.fitness.admin.dashboard.vo.DashboardOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板控制器
 */
@Tag(name = "数据看板")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取数据看板概览")
    @GetMapping("/overview")
    public R<DashboardOverviewVO> overview(@RequestParam(defaultValue = "7") Integer days) {
        return success(dashboardService.getOverview(days));
    }
}
