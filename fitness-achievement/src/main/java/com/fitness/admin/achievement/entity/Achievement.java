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
    private String iconUrl;
    private String conditionType;
    private Integer conditionValue;
    private String badgeColor;
}
