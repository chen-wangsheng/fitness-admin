package com.fitness.admin.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report")
public class Report extends BaseEntity {

    private Long reporterId;
    private Long targetId;
    private String targetType;
    private String reason;
    private String description;
    private String images;
    private Integer status;
    private String handleResult;
    private Long handlerId;
}
