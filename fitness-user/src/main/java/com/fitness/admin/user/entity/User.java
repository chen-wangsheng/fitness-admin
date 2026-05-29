package com.fitness.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private Integer height;
    private Integer weight;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer status;
    private String phone;
    private String aiProfile;
    private Integer totalWorkouts;
    private Integer totalDuration;
    private LocalDate lastWorkoutDate;
}
