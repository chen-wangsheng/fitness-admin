package com.fitness.admin.workout.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workout_log_set")
public class WorkoutLogSet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workoutLogExerciseId;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weight;
    private Integer duration;
    private Integer completed;
    private LocalDateTime createdAt;
}
