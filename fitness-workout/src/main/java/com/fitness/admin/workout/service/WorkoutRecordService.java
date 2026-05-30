package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.workout.dto.WorkoutQueryDTO;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.mapper.WorkoutLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutRecordService {

    private final WorkoutLogMapper workoutLogMapper;

    public Page<WorkoutLog> queryPage(WorkoutQueryDTO queryDTO) {
        Page<WorkoutLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            wrapper.eq(WorkoutLog::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(WorkoutLog::getWorkoutDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(WorkoutLog::getWorkoutDate, queryDTO.getEndDate());
        }

        wrapper.orderByDesc(WorkoutLog::getWorkoutDate);
        return workoutLogMapper.selectPage(page, wrapper);
    }
}
