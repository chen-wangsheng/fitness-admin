package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_prompt_template")
public class AiPromptTemplate extends BaseEntity {

    private String name;
    private String code;
    private String content;
    private String description;
    private Integer version;
    private Integer status;
}
