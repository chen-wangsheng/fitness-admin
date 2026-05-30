package com.fitness.admin.workout.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkoutPeakHoursVO {

    private List<Integer> hours;
    private List<String> days;
    private List<int[]> values;
}
