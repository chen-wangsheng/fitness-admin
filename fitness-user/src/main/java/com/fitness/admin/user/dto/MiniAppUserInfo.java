package com.fitness.admin.user.dto;

import lombok.Data;

/**
 * 小程序用户信息
 */
@Data
public class MiniAppUserInfo {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private String fitnessGoal;
    private String fitnessLevel;
    private Long currentPlanId;
    private Integer status;
}
