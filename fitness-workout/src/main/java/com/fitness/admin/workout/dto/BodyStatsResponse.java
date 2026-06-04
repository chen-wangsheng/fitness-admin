package com.fitness.admin.workout.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 身体数据统计响应
 */
@Data
public class BodyStatsResponse {
    private BigDecimal currentWeight;
    private BigDecimal targetWeight;
    private BigDecimal weightChange;
    private BigDecimal bodyFatPct;
    private BigDecimal bmi;
    private List<Milestone> milestones;

    // 趋势数据
    private List<TrendItem> weightTrend;
    private List<TrendItem> fatTrend;
    private List<TrendItem> muscleTrend;
    private List<TrendItem> bmiTrend;

    @Data
    public static class TrendItem {
        private String date;
        private BigDecimal value;

        public TrendItem(String date, BigDecimal value) {
            this.date = date;
            this.value = value;
        }
    }
}
