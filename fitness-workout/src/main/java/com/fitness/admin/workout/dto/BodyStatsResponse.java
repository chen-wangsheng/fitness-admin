package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 身体数据统计响应
 */
@Data
public class BodyStatsResponse {
    private BigDecimal currentWeight;
    private BigDecimal targetWeight;
    private BigDecimal weightChange;
    private BigDecimal bodyFatPct;
    private BigDecimal bmi;
    private List<Milestone> milestones;
}
