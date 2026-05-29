package com.fitness.admin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("plan_day_exercise")
public class PlanDayExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long planDayId;
    private Long exerciseId;
    private Integer sets;
    private Integer reps;
    private Integer duration;
    private Integer restSeconds;
    private Integer sort;
}
