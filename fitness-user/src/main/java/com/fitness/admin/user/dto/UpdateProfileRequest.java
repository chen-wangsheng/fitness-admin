package com.fitness.admin.user.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新用户信息请求
 */
@Data
public class UpdateProfileRequest {
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private String birthday;
    private BigDecimal heightCm;
    private BigDecimal currentWeightKg;
    private BigDecimal targetWeightKg;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMin;
}
