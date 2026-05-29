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
    private BigDecimal weight;
    private BigDecimal bodyFat;
    private BigDecimal muscleMass;
    private BigDecimal bmi;
    private Integer chest;
    private Integer waist;
    private Integer hips;
    private Integer arm;
    private Integer thigh;
    private String notes;
}
