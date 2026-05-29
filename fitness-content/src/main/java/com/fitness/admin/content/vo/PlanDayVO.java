package com.fitness.admin.content.vo;

import lombok.Data;

import java.util.List;

@Data
public class PlanDayVO {

    private Long id;
    private Integer weekNumber;
    private Integer dayNumber;
    private String focus;
    private String description;
    private List<PlanExerciseVO> exercises;
}
