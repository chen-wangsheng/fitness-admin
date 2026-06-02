package com.fitness.admin.workout.dto;

import lombok.Data;

/**
 * 开始训练请求
 */
@Data
public class StartWorkoutRequest {
    /** 计划ID */
    private Long planId;
    /** 计划日ID */
    private Long planDayId;
}
