package com.fitness.admin.content.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ExerciseCreateDTO {

    private String name;
    private String nameEn;
    private Integer categoryId;
    private String description;
    private String instructions;
    private String tips;
    private String demoImageUrl;
    private String demoVideoUrl;
    private String exerciseType;
    private String equipment;
    private String difficulty;
    private BigDecimal caloriesPerRep;
    private BigDecimal caloriesPerMin;
    private Integer isCompound;
    private Integer status;
    private List<Long> bodyPartIds;
}
