package com.fitness.admin.workout.dto;

import lombok.Data;

import java.util.List;

/**
 * 开始训练响应
 */
@Data
public class StartWorkoutResponse {
    private Long workoutLogId;
    private String startTime;
    private List<ExerciseInfo> exercises;
}
