package com.fitness.admin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workout_plan")
public class WorkoutPlan extends BaseEntity {

    private String name;
    private String description;
    private String coverImageUrl;
    private String difficultyLevel;
    private String fitnessGoal;
    private Integer durationWeeks;
    private Integer daysPerWeek;
    private Integer avgDurationMin;
    private Integer isSystem;
    private Long createdBy;
    private Integer aiGenerated;
    private String aiGenerationParams;
    private Integer status;
}
