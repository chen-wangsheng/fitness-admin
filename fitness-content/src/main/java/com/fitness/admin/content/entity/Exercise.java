package com.fitness.admin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exercise")
public class Exercise extends BaseEntity {

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
}
