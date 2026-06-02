package com.fitness.admin.workout.dto;

import lombok.Data;

/**
 * 完成训练请求
 */
@Data
public class CompleteWorkoutRequest {
    /** 感受评分 1-5 */
    private Integer feelingScore;
    /** RPE 1-10 */
    private Integer rpe;
    /** 备注 */
    private String notes;
}
