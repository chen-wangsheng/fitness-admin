package com.fitness.admin.achievement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("achievement")
public class Achievement extends BaseEntity {

    private String name;
    private String description;
    private String icon;
    private String type;
    private Integer requirement;
    private Integer status;
    private Integer sort;
}
