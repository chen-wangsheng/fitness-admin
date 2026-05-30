package com.fitness.admin.workout.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.workout.dto.AnalyticsQueryDTO;
import com.fitness.admin.workout.dto.WorkoutQueryDTO;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.service.WorkoutRecordService;
import com.fitness.admin.workout.vo.ExercisePopularityVO;
import com.fitness.admin.workout.vo.PlanFunnelVO;
import com.fitness.admin.workout.vo.WorkoutOverviewVO;
import com.fitness.admin.workout.vo.WorkoutPeakHoursVO;
import com.fitness.admin.workout.vo.WorkoutTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "训练记录管理")
@RestController
@RequestMapping("/workout-record")
@RequiredArgsConstructor
public class WorkoutRecordController extends BaseController {

    private final WorkoutRecordService workoutRecordService;

    @Operation(summary = "训练记录列表")
    @GetMapping("/list")
    public R<PageResult<WorkoutLog>> list(WorkoutQueryDTO queryDTO) {
        Page<WorkoutLog> page = workoutRecordService.queryPage(queryDTO);
        return page((Page) page);
    }

    @Operation(summary = "训练概览数据")
    @GetMapping("/overview")
    public R<WorkoutOverviewVO> overview(AnalyticsQueryDTO queryDTO) {
        return success(workoutRecordService.getOverview(queryDTO));
    }

    @Operation(summary = "训练趋势数据")
    @GetMapping("/trend")
    public R<WorkoutTrendVO> trend(AnalyticsQueryDTO queryDTO) {
        return success(workoutRecordService.getTrend(queryDTO));
    }

    @Operation(summary = "高峰时段热力图数据")
    @GetMapping("/peak-hours")
    public R<WorkoutPeakHoursVO> peakHours(AnalyticsQueryDTO queryDTO) {
        return success(workoutRecordService.getPeakHours(queryDTO));
    }

    @Operation(summary = "热门动作排行")
    @GetMapping("/exercise-popularity")
    public R<List<ExercisePopularityVO>> exercisePopularity(AnalyticsQueryDTO queryDTO) {
        return success(workoutRecordService.getExercisePopularity(queryDTO));
    }

    @Operation(summary = "计划漏斗数据")
    @GetMapping("/plan-funnel")
    public R<PlanFunnelVO> planFunnel(AnalyticsQueryDTO queryDTO) {
        return success(workoutRecordService.getPlanFunnel(queryDTO));
    }
}
