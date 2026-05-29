package com.fitness.admin.workout.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutQueryDTO {

    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
