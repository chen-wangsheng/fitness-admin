package com.fitness.admin.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;
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
    private Integer totalWorkouts;
    private Integer totalDuration;
    private LocalDate lastWorkoutDate;
    private List<UserTagVO> tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
