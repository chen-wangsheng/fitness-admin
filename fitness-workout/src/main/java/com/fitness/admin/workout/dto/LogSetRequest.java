package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 记录一组数据请求
 */
@Data
public class LogSetRequest {
    /** 训练记录动作ID */
    private Long logExerciseId;
    /** 组号 */
    private Integer setNumber;
    /** 组类型: normal/warmup/dropset */
    private String setType;
    /** 重量(kg) */
    private BigDecimal weightKg;
    /** 次数 */
    private Integer reps;
    /** 是否完成 */
    private Boolean isCompleted;
    /** 备注 */
    private String notes;
}
