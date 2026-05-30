package com.fitness.admin.workout.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkoutTrendVO {

    private List<String> dates;
    private List<Integer> counts;
    private List<Long> volumes;
}
