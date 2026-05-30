package com.fitness.admin.workout.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkoutOverviewVO {

    private Long totalWorkouts;
    private BigDecimal completionRate;
    private Integer avgDuration;
    private BigDecimal totalVolume;
}
