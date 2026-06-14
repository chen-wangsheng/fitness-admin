package com.fitness.admin.user.dto;

import jakarta.validation.constraints.Pattern;
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
    /**
     * 邮箱(选填),用于接收系统公告邮件。传空字符串视为清空。
     * 简单宽松校验,不强制 RFC,符合常规邮箱格式即可。
     */
    @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "邮箱格式不正确")
    private String email;
}
