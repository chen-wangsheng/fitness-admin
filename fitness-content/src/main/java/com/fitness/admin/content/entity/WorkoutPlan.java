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
    private String coverImage;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer durationWeeks;
    private Integer daysPerWeek;
    private Integer difficulty;
    private Integer status;
    private Integer sort;
    private Long createBy;
    private Integer aiGenerated;
}
