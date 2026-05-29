package com.fitness.admin.workout.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkoutExerciseVO {

    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private Integer duration;
    private Integer restSeconds;
    private List<WorkoutSetVO> setList;
}
