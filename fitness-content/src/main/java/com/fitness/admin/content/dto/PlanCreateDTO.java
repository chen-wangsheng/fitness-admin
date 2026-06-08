package com.fitness.admin.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlanCreateDTO {

    private String name;
    private String description;
    private String coverImageUrl;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer durationWeeks;
    private Integer daysPerWeek;
    private Integer difficulty;
    private Integer status;
    private List<PlanDayDTO> days;

    @Data
    public static class PlanDayDTO {
        private Integer weekNumber;
        private Integer dayNumber;
        private String focus;
        private String description;
        private List<PlanExerciseDTO> exercises;
    }

    @Data
    public static class PlanExerciseDTO {
        private Long exerciseId;
        private Integer sets;
        private String reps;
        private Integer duration;
        private Integer restSeconds;
        private Integer sort;
    }
}
