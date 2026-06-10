package com.fitness.admin.workout.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    /** SQL列: log_exercise_id */
    @TableField("log_exercise_id")
    private Long workoutLogExerciseId;

    private Integer setNumber;
    private Integer reps;

    /** SQL列: weight_kg */
    @TableField("weight_kg")
    private BigDecimal weight;

    /** SQL列: duration_sec */
    @TableField("duration_sec")
    private Integer duration;

    /** SQL列: is_completed */
    @TableField("is_completed")
    private Integer completed;

    /** SQL列: is_pr */
    @TableField("is_pr")
    private Integer isPr;

    private LocalDateTime createdAt;
}
