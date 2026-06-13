package com.fitness.admin.workout.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.workout.dto.BodyStatsResponse;
import com.fitness.admin.workout.dto.MilestoneResponse;
import com.fitness.admin.workout.dto.RecordBodyRequest;
import com.fitness.admin.workout.service.MiniAppBodyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序身体数据接口
 */
@Tag(name = "小程序-身体数据模块")
@RestController
@RequestMapping("/miniapp/body")
@RequiredArgsConstructor
@SaCheckLogin
public class MiniAppBodyController extends BaseController {

    private final MiniAppBodyService miniAppBodyService;

    @Operation(summary = "身体数据统计")
    @GetMapping("/stats")
    public R<BodyStatsResponse> getStats() {
        return success(miniAppBodyService.getStats());
    }

    @Operation(summary = "记录身体数据")
    @PostMapping("/record")
    public R<Void> record(@RequestBody RecordBodyRequest request) {
        miniAppBodyService.record(request);
        return success();
    }

    @Operation(summary = "里程碑列表")
    @GetMapping("/milestones")
    public R<MilestoneResponse> getMilestones() {
        return success(miniAppBodyService.getMilestones());
    }

    @Operation(summary = "身体数据趋势")
    @GetMapping("/trend")
    public R<Map<String, Object>> getTrend(
            @RequestParam String metric,
            @RequestParam(defaultValue = "30") Integer days) {
        return success(miniAppBodyService.getTrend(metric, days));
    }
}
