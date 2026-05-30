package com.fitness.admin.workout.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExercisePopularityVO {

    private String name;
    private Long count;
    private BigDecimal totalVolume;
    private BigDecimal avgWeight;
    private Long userCount;
}
