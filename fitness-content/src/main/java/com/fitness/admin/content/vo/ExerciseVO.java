package com.fitness.admin.content.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExerciseVO {

    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private List<String> instructions;
    private List<String> tips;
    private String demoImageUrl;
    private String demoVideoUrl;
    private String exerciseType;
    private String equipment;
    private String difficulty;
    private BigDecimal caloriesPerRep;
    private BigDecimal caloriesPerMin;
    private Integer isCompound;
    private Integer status;
    private List<BodyPartVO> bodyParts;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
