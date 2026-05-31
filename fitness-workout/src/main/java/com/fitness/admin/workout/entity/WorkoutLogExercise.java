package com.fitness.admin.workout.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("workout_log_exercise")
public class WorkoutLogExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long workoutLogId;
    private Long exerciseId;
    private Integer sortOrder;
}
