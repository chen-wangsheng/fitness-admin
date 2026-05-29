package com.fitness.admin.workout.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.workout.dto.WorkoutQueryDTO;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.service.WorkoutRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
