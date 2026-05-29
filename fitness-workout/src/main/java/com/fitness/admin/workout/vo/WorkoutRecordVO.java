package com.fitness.admin.workout.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutRecordVO {

    private Long id;
    private Long userId;
    private String userNickname;
    private String name;
    private LocalDate workoutDate;
    private Integer duration;
    private Integer totalSets;
    private Integer totalReps;
    private Integer calories;
    private String mood;
    private String notes;
    private List<WorkoutExerciseVO> exercises;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
