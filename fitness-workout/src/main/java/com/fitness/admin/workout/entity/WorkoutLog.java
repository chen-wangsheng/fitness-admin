package com.fitness.admin.workout.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workout_log")
public class WorkoutLog extends BaseEntity {

    private Long userId;
    private Long planId;
    private Long planDayId;
    private String name;
    private LocalDate workoutDate;
    private Integer duration;
    private Integer totalSets;
    private Integer totalReps;
    private Integer calories;
    private String mood;
    private String notes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
