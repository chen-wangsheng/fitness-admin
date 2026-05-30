package com.fitness.admin.content.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlanVO {

    private Long id;
    private String name;
    private String description;
    private String coverImageUrl;
    private String difficultyLevel;
    private String fitnessGoal;
    private Integer durationWeeks;
    private Integer daysPerWeek;
    private Integer avgDurationMin;
    private Integer isSystem;
    private Long createdBy;
    private Integer aiGenerated;
    private Integer status;
    private List<PlanDayVO> days;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
