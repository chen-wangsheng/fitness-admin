package com.fitness.admin.content.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExerciseVO {

    private Long id;
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
    private List<BodyPartVO> bodyParts;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
