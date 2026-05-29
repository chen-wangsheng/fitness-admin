package com.fitness.admin.content.dto;

import lombok.Data;

@Data
public class ExerciseQueryDTO {

    private String keyword;
    private String exerciseType;
    private String equipment;
    private String difficulty;
    private Integer status;
    private Long bodyPartId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
