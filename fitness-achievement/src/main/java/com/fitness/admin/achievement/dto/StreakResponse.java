package com.fitness.admin.achievement.dto;

import lombok.Data;

/**
 * 连续打卡响应
 */
@Data
public class StreakResponse {
    private Integer currentStreak;
    private Integer longestStreak;
    private String lastCheckinDate;
}
