package com.fitness.admin.ai.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.entity.AiUsageDaily;
import com.fitness.admin.ai.mapper.AiChatMessageMapper;
import com.fitness.admin.ai.mapper.AiChatSessionMapper;
import com.fitness.admin.ai.mapper.AiPlanMapper;
import com.fitness.admin.ai.mapper.AiUsageDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * AI每日使用统计聚合任务
 * 每天凌晨2点从原始业务表聚合前一天的AI使用数据，写入 ai_usage_daily 表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiUsageDailyJob {

    private final AiUsageDailyMapper aiUsageDailyMapper;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiPlanMapper aiPlanMapper;

    /**
     * 每天凌晨2点执行，聚合前一天的AI使用统计
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateDailyUsage() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始聚合AI每日使用统计: date={}", yesterday);
        try {
            aggregateForDate(yesterday);
            log.info("AI每日使用统计聚合完成: date={}", yesterday);
        } catch (Exception e) {
            log.error("AI每日使用统计聚合失败: date={}, error={}", yesterday, e.getMessage(), e);
        }
    }

    /**
     * 聚合指定日期的AI使用统计
     */
    public void aggregateForDate(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        // 1. 统计当天新建的会话数
        Long chatSessions = aiChatSessionMapper.selectCount(
                Wrappers.<AiChatSession>lambdaQuery()
                        .between(AiChatSession::getCreatedAt, dayStart, dayEnd)
        );

        // 2. 统计当天的消息数
        List<AiChatMessage> dayMessages = aiChatMessageMapper.selectList(
                Wrappers.<AiChatMessage>lambdaQuery()
                        .between(AiChatMessage::getCreatedAt, dayStart, dayEnd)
        );
        int totalMessages = dayMessages.size();

        // 3. 统计正面/负面反馈
        int positiveFeedback = 0;
        int negativeFeedback = 0;
        for (AiChatMessage msg : dayMessages) {
            if (msg.getFeedback() != null) {
                if (msg.getFeedback() > 0) positiveFeedback++;
                else if (msg.getFeedback() < 0) negativeFeedback++;
            }
        }

        // 4. 计算满意度
        int feedbackTotal = positiveFeedback + negativeFeedback;
        BigDecimal satisfactionRate = feedbackTotal > 0
                ? BigDecimal.valueOf(positiveFeedback)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(feedbackTotal), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 5. 统计Token使用量（assistant消息的tokenCount之和）
        long totalTokens = dayMessages.stream()
                .filter(m -> "assistant".equals(m.getRole()) && m.getTokenCount() != null)
                .mapToLong(AiChatMessage::getTokenCount)
                .sum();

        // 6. 统计当天生成的计划数
        Long planGenerated = aiPlanMapper.selectCount(
                Wrappers.<AiPlan>lambdaQuery()
                        .between(AiPlan::getCreatedAt, dayStart, dayEnd)
        );

        // 7. 统计当天确认的计划数
        Long planConfirmed = aiPlanMapper.selectCount(
                Wrappers.<AiPlan>lambdaQuery()
                        .eq(AiPlan::getStatus, "confirmed")
                        .between(AiPlan::getCreatedAt, dayStart, dayEnd)
        );

        // 8. 计算RAG命中率（assistant消息中有ragRefs的比例）
        long assistantMessages = dayMessages.stream()
                .filter(m -> "assistant".equals(m.getRole()))
                .count();
        long ragHitMessages = dayMessages.stream()
                .filter(m -> "assistant".equals(m.getRole())
                        && m.getRagRefs() != null && !m.getRagRefs().isBlank())
                .count();
        BigDecimal ragHitRate = assistantMessages > 0
                ? BigDecimal.valueOf(ragHitMessages)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(assistantMessages), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 9. 平均响应时间（基于assistant消息的tokenCount估算，约10ms/token）
        int avgResponseTimeMs = 0;
        if (assistantMessages > 0) {
            long avgTokens = dayMessages.stream()
                    .filter(m -> "assistant".equals(m.getRole()) && m.getTokenCount() != null)
                    .mapToLong(AiChatMessage::getTokenCount)
                    .sum() / Math.max(assistantMessages, 1);
            avgResponseTimeMs = (int) (avgTokens * 10);
        }

        // 10. 写入或更新 ai_usage_daily
        AiUsageDaily existing = aiUsageDailyMapper.selectOne(
                Wrappers.<AiUsageDaily>lambdaQuery()
                        .eq(AiUsageDaily::getStatDate, date)
        );

        if (existing != null) {
            existing.setTotalChatSessions(chatSessions.intValue());
            existing.setTotalChatMessages(totalMessages);
            existing.setTotalPlanGenerated(planGenerated.intValue());
            existing.setTotalPlanConfirmed(planConfirmed.intValue());
            existing.setTotalTokensUsed(totalTokens);
            existing.setPositiveFeedbackCount(positiveFeedback);
            existing.setNegativeFeedbackCount(negativeFeedback);
            existing.setSatisfactionRate(satisfactionRate);
            existing.setAvgResponseTimeMs(avgResponseTimeMs);
            existing.setRagHitRate(ragHitRate);
            aiUsageDailyMapper.updateById(existing);
        } else {
            AiUsageDaily record = new AiUsageDaily();
            record.setStatDate(date);
            record.setTotalChatSessions(chatSessions.intValue());
            record.setTotalChatMessages(totalMessages);
            record.setTotalPlanGenerated(planGenerated.intValue());
            record.setTotalPlanConfirmed(planConfirmed.intValue());
            record.setTotalTokensUsed(totalTokens);
            record.setPositiveFeedbackCount(positiveFeedback);
            record.setNegativeFeedbackCount(negativeFeedback);
            record.setSatisfactionRate(satisfactionRate);
            record.setAvgResponseTimeMs(avgResponseTimeMs);
            record.setRagHitRate(ragHitRate);
            record.setCreatedAt(LocalDateTime.now());
            aiUsageDailyMapper.insert(record);
        }

        log.info("AI使用统计聚合完成: date={}, sessions={}, messages={}, plans={}",
                date, chatSessions, totalMessages, planGenerated);
    }
}
