package com.fitness.admin.workout.controller;

import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.workout.dto.*;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.service.MiniAppWorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序训练记录接口
 */
@Tag(name = "小程序-训练模块")
@RestController
@RequestMapping("/miniapp/workout")
@RequiredArgsConstructor
public class MiniAppWorkoutController extends BaseController {

    private final MiniAppWorkoutService miniAppWorkoutService;

    @Operation(summary = "开始训练")
    @PostMapping("/start")
    public R<StartWorkoutResponse> startWorkout(@RequestBody StartWorkoutRequest request) {
        return success(miniAppWorkoutService.startWorkout(request));
    }

    @Operation(summary = "记录一组数据")
    @PostMapping("/log-set")
    public R<LogSetResponse> logSet(@RequestBody LogSetRequest request) {
        return success(miniAppWorkoutService.logSet(request));
    }

    @Operation(summary = "完成训练")
    @PostMapping("/complete")
    public R<CompleteWorkoutResponse> completeWorkout(@RequestBody CompleteWorkoutRequest request) {
        return success(miniAppWorkoutService.completeWorkout(request));
    }

    @Operation(summary = "训练历史")
    @GetMapping("/history")
    public R<PageResult<WorkoutLog>> history(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {
        return success(miniAppWorkoutService.getHistory(pageNum, pageSize, startDate, endDate, status));
    }

    @Operation(summary = "训练统计")
    @GetMapping("/stats")
    public R<WorkoutStatsResponse> stats(
            @RequestParam(defaultValue = "week") String period) {
        return success(miniAppWorkoutService.getStats(period));
    }
}
