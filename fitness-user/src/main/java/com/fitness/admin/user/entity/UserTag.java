package com.fitness.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_tag")
public class UserTag extends BaseEntity {

    private String name;
    private String color;
    private String description;
    private Integer sort;
    private Integer status;
}
