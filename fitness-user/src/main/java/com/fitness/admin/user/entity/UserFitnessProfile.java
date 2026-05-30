package com.fitness.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_fitness_profile")
public class UserFitnessProfile extends BaseEntity {

    private Long userId;
    private String injuries;
    private String allergies;
    private String availableEquipment;
    private String preferredWorkoutTime;
    private String trainingPreferences;
    private String healthConditions;
    private String aiNotes;
}
