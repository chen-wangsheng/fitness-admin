package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 完成训练响应
 */
@Data
public class CompleteWorkoutResponse {
    private Long workoutLogId;
    private Integer durationMin;
    private BigDecimal totalVolumeKg;
    private Integer totalSets;
    private BigDecimal estimatedCalories;
    private List<AchievementInfo> achievements;
}
