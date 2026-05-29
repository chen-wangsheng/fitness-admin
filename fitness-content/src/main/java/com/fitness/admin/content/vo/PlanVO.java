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
    private String coverImage;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer durationWeeks;
    private Integer daysPerWeek;
    private Integer difficulty;
    private Integer status;
    private Integer sort;
    private Integer aiGenerated;
    private List<PlanDayVO> days;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
