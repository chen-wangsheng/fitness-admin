package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 训练统计响应
 */
@Data
public class WorkoutStatsResponse {
    private Integer totalWorkouts;
    private Integer totalDurationMin;
    private BigDecimal totalVolumeKg;
    private BigDecimal totalCalories;
    private Integer avgDurationMin;
    private List<Integer> workoutDays;
    private Integer streakDays;
    private List<PrRecord> prRecords;
}
