package com.fitness.admin.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("body_part")
public class BodyPart extends BaseEntity {

    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
}
