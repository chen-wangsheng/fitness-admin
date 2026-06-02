package com.fitness.admin.ai.dto;

import lombok.Data;

/**
 * 确认AI计划响应
 */
@Data
public class ConfirmPlanResponse {
    private Long aiPlanId;
    private Long workoutPlanId;
    private String status;
}
