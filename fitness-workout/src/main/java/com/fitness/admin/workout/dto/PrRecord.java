package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 个人记录
 */
@Data
public class PrRecord {
    private String exerciseName;
    private String type;
    private BigDecimal value;
    private String unit;
    private String date;
}
