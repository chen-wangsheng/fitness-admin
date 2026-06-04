package com.fitness.admin.content.vo;

import lombok.Data;

import java.util.List;

@Data
public class PlanDayVO {

    private Long id;
    private Integer weekNumber;
    private Integer dayOfWeek;
    private String dayLabel;
    private String description;
    private Integer isRestDay;
    private List<PlanExerciseVO> exercises;
}
