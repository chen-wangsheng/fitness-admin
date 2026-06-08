package com.fitness.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_fitness_profile", autoResultMap = true)
public class UserFitnessProfile extends BaseEntity {

    private Long userId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> injuries;

    private String allergies;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> availableEquipment;

    private String preferredWorkoutTime;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> trainingPreferences;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> healthConditions;

    private String aiNotes;
}
