package com.fitness.admin.workout.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanFunnelVO {

    private Long totalPlans;
    private Long startedPlans;
    private Long completedPlans;
    private BigDecimal startRate;
    private BigDecimal completionRate;
}
