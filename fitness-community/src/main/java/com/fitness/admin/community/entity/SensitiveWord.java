package com.fitness.admin.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sensitive_word")
public class SensitiveWord extends BaseEntity {

    private String word;
    private String type;
    private Integer status;
}
