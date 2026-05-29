package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_safety_rule")
public class AiSafetyRule extends BaseEntity {

    private String name;
    private String description;
    private String keywords;
    private String action;
    private Integer priority;
    private Integer status;
}
