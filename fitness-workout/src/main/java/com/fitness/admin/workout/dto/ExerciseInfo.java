package com.fitness.admin.workout.dto;

import lombok.Data;

/**
 * 训练动作信息
 */
@Data
public class ExerciseInfo {
    private Long logExerciseId;
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Integer restSeconds;
    private Integer sort;
}
