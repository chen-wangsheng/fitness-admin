package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("plan_load_adjustment")
public class PlanLoadAdjustment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long aiPlanId;
    private Integer weekNumber;
    private String adjustmentType;
    private String adjustmentReason;
    private String metricsSnapshot;
    private BigDecimal loadChangePct;
    private String exerciseChanges;
    private LocalDateTime createdAt;
}
