package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_prompt_template")
public class AiPromptTemplate extends BaseEntity {

    private String name;
    private String templateKey;
    private String content;
    private String variables;
    private Integer version;
    private Integer isActive;
    private LocalDateTime activatedAt;
}
