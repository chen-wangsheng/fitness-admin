package com.fitness.admin.workout.dto;

import lombok.Data;

/**
 * 里程碑
 */
@Data
public class Milestone {
    private Long id;
    private String type;
    private String title;
    private String description;
    private String achievedAt;
    private String iconUrl;
}
