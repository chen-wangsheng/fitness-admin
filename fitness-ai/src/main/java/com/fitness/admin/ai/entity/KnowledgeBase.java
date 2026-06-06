package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "knowledge_base", autoResultMap = true)
public class KnowledgeBase extends BaseEntity {

    private String title;
    private String content;
    private String category;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private String source;
    private String vectorStatus;
    private String vectorId;
    private Integer status;
}
