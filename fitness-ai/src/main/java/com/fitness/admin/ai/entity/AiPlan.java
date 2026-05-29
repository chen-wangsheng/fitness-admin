package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_plan")
public class AiPlan extends BaseEntity {

    private Long userId;
    private String prompt;
    private String response;
    private Integer status;
    private Integer converted;
    private Long convertedPlanId;
}
