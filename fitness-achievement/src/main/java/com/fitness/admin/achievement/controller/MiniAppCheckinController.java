package com.fitness.admin.achievement.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fitness.admin.achievement.dto.*;
import com.fitness.admin.achievement.service.MiniAppCheckinService;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序打卡接口
 */
@Tag(name = "小程序-打卡模块")
@RestController
@RequestMapping("/miniapp/checkin")
@RequiredArgsConstructor
@SaCheckLogin
public class MiniAppCheckinController extends BaseController {

    private final MiniAppCheckinService miniAppCheckinService;

    @Operation(summary = "每日打卡")
    @PostMapping
    public R<CheckinResponse> checkin(@RequestBody CheckinRequest request) {
        return success(miniAppCheckinService.checkin(request));
    }

    @Operation(summary = "打卡统计")
    @GetMapping("/stats")
    public R<CheckinStatsResponse> getStats(
            @RequestParam(defaultValue = "30") Integer days) {
        return success(miniAppCheckinService.getStats(days));
    }

    @Operation(summary = "连续打卡天数")
    @GetMapping("/streak")
    public R<StreakResponse> getStreak() {
        return success(miniAppCheckinService.getStreak());
    }

    @Operation(summary = "成就列表")
    @GetMapping("/achievements")
    public R<AchievementListResponse> getAchievements() {
        return success(miniAppCheckinService.getAchievements());
    }
}
