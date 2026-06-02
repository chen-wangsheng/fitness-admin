package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 记录身体数据请求
 */
@Data
public class RecordBodyRequest {
    private String recordDate;
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
