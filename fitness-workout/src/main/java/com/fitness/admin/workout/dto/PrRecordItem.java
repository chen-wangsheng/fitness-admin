package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 个人最佳记录项
 */
@Data
public class PrRecordItem {

    /**
     * 动作ID
     */
    private Long exerciseId;

    /**
     * 动作名称
     */
    private String exerciseName;

    /**
     * 动作演示图
     */
    private String demoImageUrl;

    /**
     * 最大重量(kg)
     */
    private BigDecimal maxWeight;

    /**
     * 最大重量对应的次数
     */
    private Integer reps;

    /**
     * 达成日期
     */
    private LocalDate achievedDate;
}
