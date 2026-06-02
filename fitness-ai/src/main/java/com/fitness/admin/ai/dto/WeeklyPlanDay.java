package com.fitness.admin.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 周计划日
 */
@Data
public class WeeklyPlanDay {
    private Integer dayOfWeek;
    private String dayLabel;
    private List<PlanExercise> exercises;
}
