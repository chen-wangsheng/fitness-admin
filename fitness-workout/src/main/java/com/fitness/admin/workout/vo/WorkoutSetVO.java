package com.fitness.admin.workout.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkoutSetVO {

    private Long id;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weight;
    private Integer duration;
    private Integer completed;
}
