package com.fitness.admin.achievement.dto;

import lombok.Data;

import java.util.List;

/**
 * 打卡统计响应
 */
@Data
public class CheckinStatsResponse {
    private Integer totalCheckins;
    private Integer days;
    private Double checkinRate;
    private List<DailyStat> dailyStats;
}
