package com.fitness.admin.achievement.dto;

import lombok.Data;

/**
 * 打卡响应
 */
@Data
public class CheckinResponse {
    private Long checkinId;
    private String checkinDate;
    private Integer streakDays;
    private Boolean isMilestone;
}
