package com.fitness.admin.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExerciseCreateDTO {

    private String name;
    private String description;
    private String instructions;
    private String tips;
    private String videoUrl;
    private String coverImage;
    private String exerciseType;
    private String equipment;
    private String difficulty;
    private Integer duration;
    private Integer calories;
    private Integer status;
    private List<Long> bodyPartIds;
}
