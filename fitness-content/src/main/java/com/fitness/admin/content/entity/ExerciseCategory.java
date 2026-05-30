package com.fitness.admin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exercise_category")
public class ExerciseCategory extends BaseEntity {

    private String name;
    private Integer parentId;
    private Integer sortOrder;
}
