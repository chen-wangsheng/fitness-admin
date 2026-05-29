package com.fitness.admin.user.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserUpdateDTO {

    private Long id;
    private String nickname;
    private Integer gender;
    private LocalDate birthday;
    private Integer height;
    private Integer weight;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer status;
    private List<Long> tagIds;
}
