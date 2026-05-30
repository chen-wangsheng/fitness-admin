package com.fitness.admin.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal heightCm;
    private BigDecimal currentWeightKg;
    private BigDecimal targetWeightKg;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer workoutDaysPerWeek;
    private Integer workoutDurationMin;
    private Integer statusCode;
    private List<UserTagVO> tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonGetter("status")
    public String getStatus() {
        if (statusCode == null) return "disabled";
        return statusCode == 1 ? "active" : "disabled";
    }
}
