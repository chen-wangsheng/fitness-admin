package com.fitness.admin.achievement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.achievement.dto.*;
import com.fitness.admin.achievement.entity.Achievement;
import com.fitness.admin.achievement.entity.Checkin;
import com.fitness.admin.achievement.entity.UserAchievement;
import com.fitness.admin.achievement.mapper.AchievementMapper;
import com.fitness.admin.achievement.mapper.CheckinMapper;
import com.fitness.admin.achievement.mapper.UserAchievementMapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 小程序打卡服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppCheckinService {

    private final CheckinMapper checkinMapper;
    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;

    /**
     * 每日打卡
     */
    @Transactional
    public CheckinResponse checkin(CheckinRequest request) {
        Long userId = getCurrentUserId();
        LocalDate today = LocalDate.now();

        // 检查今天是否已打卡
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Checkin::getUserId, userId)
               .eq(Checkin::getCheckinDate, today);
        Long count = checkinMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ResultCodeEnum.CHECKIN_ALREADY_DONE);
        }

        // 创建打卡记录
        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setWorkoutLogId(request.getWorkoutLogId());
        checkin.setCheckinType(request.getCheckinType() != null ? request.getCheckinType() : "workout");
        checkin.setCreatedAt(LocalDateTime.now());
        checkin.setUpdatedAt(LocalDateTime.now());
        checkinMapper.insert(checkin);

        // 计算连续打卡天数
        int streakDays = calculateStreakDays(userId);

        // 检查是否是里程碑
        boolean isMilestone = (streakDays == 7 || streakDays == 30 || streakDays == 100);

        CheckinResponse response = new CheckinResponse();
        response.setCheckinId(checkin.getId());
        response.setCheckinDate(today.toString());
        response.setStreakDays(streakDays);
        response.setIsMilestone(isMilestone);

        return response;
    }

    /**
     * 获取打卡统计
     */
    public CheckinStatsResponse getStats(Integer days) {
        Long userId = getCurrentUserId();
        LocalDate startDate = LocalDate.now().minusDays(days);

        // 查询打卡记录
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Checkin::getUserId, userId)
               .ge(Checkin::getCheckinDate, startDate);
        List<Checkin> checkins = checkinMapper.selectList(wrapper);

        // 构建每日统计
        List<DailyStat> dailyStats = new ArrayList<>();
        int checkedDays = 0;
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(days - 1 - i);
            boolean checked = checkins.stream()
                    .anyMatch(c -> c.getCheckinDate().equals(date));
            if (checked) {
                checkedDays++;
            }
            DailyStat stat = new DailyStat();
            stat.setDate(date.toString());
            stat.setChecked(checked);
            dailyStats.add(stat);
        }

        CheckinStatsResponse response = new CheckinStatsResponse();
        response.setTotalCheckins(checkedDays);
        response.setDays(days);
        response.setCheckinRate(days > 0 ? (double) checkedDays / days * 100 : 0);
        response.setDailyStats(dailyStats);

        return response;
    }

    /**
     * 获取成就列表
     */
    public AchievementListResponse getAchievements() {
        Long userId = getCurrentUserId();

        // 查询所有成就
        List<Achievement> allAchievements = achievementMapper.selectList(null);

        // 查询用户已解锁的成就
        LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
        uaWrapper.eq(UserAchievement::getUserId, userId);
        List<UserAchievement> unlocked = userAchievementMapper.selectList(uaWrapper);
        Set<Long> unlockedIds = unlocked.stream()
                .map(UserAchievement::getAchievementId)
                .collect(java.util.stream.Collectors.toSet());

        // 按conditionType分组
        Map<String, List<Achievement>> grouped = allAchievements.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getConditionType() != null ? a.getConditionType() : "other"));

        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, List<Achievement>> entry : grouped.entrySet()) {
            Map<String, Object> category = new HashMap<>();
            category.put("type", entry.getKey());
            category.put("achievements", entry.getValue().stream().map(a -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", a.getId());
                item.put("name", a.getName());
                item.put("description", a.getDescription());
                item.put("iconUrl", a.getIconUrl());
                item.put("badgeColor", a.getBadgeColor());
                item.put("unlocked", unlockedIds.contains(a.getId()));
                return item;
            }).toList());
            categories.add(category);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allAchievements.size());
        stats.put("unlocked", unlockedIds.size());
        stats.put("progress", allAchievements.isEmpty() ? 0 :
                (double) unlockedIds.size() / allAchievements.size() * 100);

        AchievementListResponse response = new AchievementListResponse();
        response.setCategories(categories);
        response.setStats(stats);
        return response;
    }

    /**
     * 获取连续打卡天数
     */
    public StreakResponse getStreak() {
        Long userId = getCurrentUserId();

        int currentStreak = calculateStreakDays(userId);
        int longestStreak = calculateLongestStreakDays(userId);

        // 获取最后打卡日期
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Checkin::getUserId, userId)
               .orderByDesc(Checkin::getCheckinDate)
               .last("LIMIT 1");
        Checkin lastCheckin = checkinMapper.selectOne(wrapper);

        StreakResponse response = new StreakResponse();
        response.setCurrentStreak(currentStreak);
        response.setLongestStreak(longestStreak);
        response.setLastCheckinDate(lastCheckin != null ? lastCheckin.getCheckinDate().toString() : null);

        return response;
    }

    /**
     * 计算连续打卡天数
     */
    private int calculateStreakDays(Long userId) {
        int streak = 0;
        LocalDate date = LocalDate.now();

        while (true) {
            LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Checkin::getUserId, userId)
                   .eq(Checkin::getCheckinDate, date);
            Long count = checkinMapper.selectCount(wrapper);
            if (count > 0) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * 计算最长连续打卡天数
     */
    private int calculateLongestStreakDays(Long userId) {
        // TODO: 实现最长连续打卡计算
        return calculateStreakDays(userId);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userId;
    }
}
