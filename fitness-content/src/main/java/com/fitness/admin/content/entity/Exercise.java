package com.fitness.admin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exercise")
public class Exercise extends BaseEntity {

    private String name;
    private String nameEn;
    private Integer categoryId;
    private String difficulty;
    private String exerciseType;
    private String equipment;
    private String description;
    private String instructions;
    private String tips;
    private String demoImageUrl;
    private String demoVideoUrl;
    private BigDecimal caloriesPerRep;
    private BigDecimal caloriesPerMin;
    private Integer isCompound;
    private Integer status;
}
