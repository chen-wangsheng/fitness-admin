package com.fitness.admin.content.vo;

import lombok.Data;

@Data
public class PlanExerciseVO {

    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Integer duration;
    private Integer restSeconds;
    private Integer sort;
}
