package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_safety_rule")
public class AiSafetyRule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleType;
    private String matchMode;
    private String pattern;
    private String action;
    private String responseTemplate;
    private String description;
    private Integer priority;
    private Integer isEnabled;
    private Integer regexTimeoutMs;
}
