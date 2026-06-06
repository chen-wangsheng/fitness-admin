package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI每日使用统计
 */
@Data
@TableName("ai_usage_daily")
public class AiUsageDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 总对话会话数
     */
    private Integer totalChatSessions;

    /**
     * 总对话消息数
     */
    private Integer totalChatMessages;

    /**
     * 总计划生成数
     */
    private Integer totalPlanGenerated;

    /**
     * 总计划确认数
     */
    private Integer totalPlanConfirmed;

    /**
     * 总token使用量
     */
    private Long totalTokensUsed;

    /**
     * 正面反馈数
     */
    private Integer positiveFeedbackCount;

    /**
     * 负面反馈数
     */
    private Integer negativeFeedbackCount;

    /**
     * 满意度
     */
    private BigDecimal satisfactionRate;

    /**
     * 平均响应时间(ms)
     */
    private Integer avgResponseTimeMs;

    /**
     * RAG命中率
     */
    private BigDecimal ragHitRate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
