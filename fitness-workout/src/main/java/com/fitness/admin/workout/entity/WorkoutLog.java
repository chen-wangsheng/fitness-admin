package com.fitness.admin.workout.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("workout_log")
public class WorkoutLog extends BaseEntity {

    private Long userId;
    private Long planId;
    private Long planDayId;
    private LocalDate workoutDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMin;
    private BigDecimal totalVolumeKg;
    private Integer totalSets;
    private BigDecimal estimatedCalories;
    private String notes;
    private Integer feelingScore;
    private Integer rpe;
    private String status;
}
