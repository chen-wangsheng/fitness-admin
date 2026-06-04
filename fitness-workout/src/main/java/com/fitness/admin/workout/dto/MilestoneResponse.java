package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 里程碑响应
 */
@Data
public class MilestoneResponse {
    private List<Milestone> milestones;
    private Integer totalDays;
    private BigDecimal latestWeight;
    private BigDecimal targetWeight;
}
