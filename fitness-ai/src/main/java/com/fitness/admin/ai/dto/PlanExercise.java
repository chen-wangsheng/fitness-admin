package com.fitness.admin.ai.dto;

import lombok.Data;

/**
 * 计划动作
 */
@Data
public class PlanExercise {
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private String reps;
    private Integer restSeconds;
}
