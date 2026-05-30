package com.fitness.admin.workout.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AnalyticsQueryDTO {

    private LocalDate startDate;
    private LocalDate endDate;
}
