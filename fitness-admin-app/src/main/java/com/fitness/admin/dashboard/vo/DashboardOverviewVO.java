package com.fitness.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据看板概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据看板概览")
public class DashboardOverviewVO {

    @Schema(description = "核心指标卡片")
    private List<MetricCard> metricCards;

    @Schema(description = "用户增长趋势")
    private List<TrendData> userGrowthTrend;

    @Schema(description = "训练活跃趋势")
    private List<TrendData> trainingActivityTrend;

    @Schema(description = "热门动作Top10")
    private List<PopularExercise> popularExercises;

    @Schema(description = "AI使用概览")
    private AiOverview aiOverview;

    /**
     * 指标卡片
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "指标卡片")
    public static class MetricCard {

        @Schema(description = "标题")
        private String title;

        @Schema(description = "数值")
        private Long value;

        @Schema(description = "较昨日变化百分比")
        private Double change;

        @Schema(description = "图标名称")
        private String icon;

        @Schema(description = "图标颜色")
        private String iconColor;
    }

    /**
     * 趋势数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "趋势数据")
    public static class TrendData {

        @Schema(description = "日期")
        private String date;

        @Schema(description = "数值列表")
        private List<SeriesData> series;
    }

    /**
     * 系列数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "系列数据")
    public static class SeriesData {

        @Schema(description = "系列名称")
        private String name;

        @Schema(description = "数值")
        private Long value;
    }

    /**
     * 热门动作
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "热门动作")
    public static class PopularExercise {

        @Schema(description = "排名")
        private Integer rank;

        @Schema(description = "动作名称")
        private String name;

        @Schema(description = "分类")
        private String category;

        @Schema(description = "部位")
        private String bodyPart;

        @Schema(description = "完成次数")
        private Long count;

        @Schema(description = "趋势")
        private Integer trend;
    }

    /**
     * AI使用概览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AI使用概览")
    public static class AiOverview {

        @Schema(description = "AI对话总量")
        private Long totalChatSessions;

        @Schema(description = "AI对话总量较昨日变化")
        private Double chatSessionsChange;

        @Schema(description = "AI计划生成数")
        private Long totalPlanGenerated;

        @Schema(description = "AI计划生成较昨日变化")
        private Double planGeneratedChange;

        @Schema(description = "知识库命中率")
        private Double ragHitRate;

        @Schema(description = "知识库命中率较昨日变化")
        private Double ragHitRateChange;

        @Schema(description = "平均响应时间(ms)")
        private Long avgResponseTime;

        @Schema(description = "平均响应时间较昨日变化")
        private Double avgResponseTimeChange;

        @Schema(description = "安全拦截次数")
        private Long safetyBlockCount;

        @Schema(description = "安全拦截较昨日变化")
        private Double safetyBlockChange;
    }
}
