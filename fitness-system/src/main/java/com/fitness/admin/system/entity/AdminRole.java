package com.fitness.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_role")
public class AdminRole extends BaseEntity {

    private String name;
    private String code;
    private String description;
    private String permissions;
    private Integer status;
}
