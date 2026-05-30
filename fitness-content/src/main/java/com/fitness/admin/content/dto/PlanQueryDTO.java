package com.fitness.admin.content.dto;

import lombok.Data;

@Data
public class PlanQueryDTO {

    private String keyword;
    private String fitnessGoal;
    private String difficultyLevel;
    private Integer status;
    private Integer aiGenerated;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
