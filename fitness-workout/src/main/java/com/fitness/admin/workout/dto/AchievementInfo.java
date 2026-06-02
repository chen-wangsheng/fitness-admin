package com.fitness.admin.workout.dto;

import lombok.Data;

/**
 * 成就信息
 */
@Data
public class AchievementInfo {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
}
