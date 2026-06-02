package com.fitness.admin.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 生成AI计划响应
 */
@Data
public class GeneratePlanResponse {
    private Long aiPlanId;
    private String splitType;
    private String explanation;
    private List<WeeklyPlanDay> weeklyPlan;
    private String disclaimer;
}
