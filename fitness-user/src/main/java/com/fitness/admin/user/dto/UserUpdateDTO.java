package com.fitness.admin.user.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UserUpdateDTO {

    private Long id;
    private String nickname;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal heightCm;
    private BigDecimal currentWeightKg;
    private BigDecimal targetWeightKg;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMin;
    private List<Long> tagIds;
}
