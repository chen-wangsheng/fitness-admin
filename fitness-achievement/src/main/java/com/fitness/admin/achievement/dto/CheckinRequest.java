package com.fitness.admin.achievement.dto;

import lombok.Data;

/**
 * 打卡请求
 */
@Data
public class CheckinRequest {
    /** 训练记录ID */
    private Long workoutLogId;
    /** 打卡类型: workout/manual */
    private String checkinType;
}
