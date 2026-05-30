package com.fitness.admin.workout.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("body_metric")
public class BodyMetric extends BaseEntity {

    private Long userId;
    private LocalDate recordDate;
    private BigDecimal weightKg;
    private BigDecimal bodyFatPct;
    private BigDecimal muscleMassKg;
    private BigDecimal bmi;
    private BigDecimal chestCm;
    private BigDecimal waistCm;
    private BigDecimal hipCm;
    private BigDecimal leftArmCm;
    private BigDecimal rightArmCm;
    private BigDecimal leftThighCm;
    private BigDecimal rightThighCm;
    private String note;
}
