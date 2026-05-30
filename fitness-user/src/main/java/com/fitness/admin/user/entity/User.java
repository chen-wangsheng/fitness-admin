package com.fitness.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal heightCm;
    private BigDecimal currentWeightKg;
    private BigDecimal targetWeightKg;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMin;
}
