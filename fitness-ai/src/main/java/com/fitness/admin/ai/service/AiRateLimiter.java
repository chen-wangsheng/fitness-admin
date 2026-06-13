package com.fitness.admin.ai.service;

import com.fitness.admin.ai.config.AiConfig;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * AI 调用限流器(每用户每分钟 N 次)。
 *
 * <p>使用 Redis 计数器:key = {@code ai:rl:{yyyyMMdd}:{userId}:{minute}},
 * 过期 90s。计数器超阈值时直接抛 {@link BizException}。
 *
 * <p>依赖 {@code StringRedisTemplate},由 {@code fitness-common} 的
 * {@code RedisConfig} 提供,无需额外配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRateLimiter {

    private static final String KEY_PREFIX = "ai:rl:";
    private static final DateTimeFormatter MINUTE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final StringRedisTemplate stringRedisTemplate;
    private final AiConfig aiConfig;

    /**
     * 检查并占用一次配额。超限时抛 {@code RATE_LIMIT} 业务异常。
     */
    public void checkAndAcquire() {
        Integer limit = aiConfig.getRateLimitPerMinute();
        if (limit == null || limit <= 0) {
            return;
        }
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return;
        }

        String key = buildKey(userId);
        Long current;
        try {
            current = stringRedisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(90));
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis 不可用,AI 限流降级为不限制: userId={}, err={}", userId, e.getMessage());
            return;
        }
        if (current != null && current > limit) {
            log.warn("AI 调用触发限流: userId={}, count={}, limit={}", userId, current, limit);
            throw new BizException(ResultCodeEnum.AI_RATE_LIMIT);
        }
    }

    private String buildKey(Long userId) {
        String minute = LocalDate.now().atStartOfDay()
                .plusMinutes(java.time.Duration.between(
                        java.time.LocalTime.MIDNIGHT,
                        java.time.LocalTime.now()).toMinutes())
                .format(MINUTE_FMT);
        return KEY_PREFIX + minute + ":" + userId;
    }
}
