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
import org.apache.ibatis.session.SqlSession;
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
    private final SqlSession sqlSession;

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
     * 获取成就列表（动态计算解锁状态，自动补解锁历史遗漏的成就）
     */
    @Transactional
    public AchievementListResponse getAchievements() {
        Long userId = getCurrentUserId();

        // 查询所有成就定义
        List<Achievement> allAchievements = achievementMapper.selectList(null);

        // 查询用户已持久化解锁的成就
        LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
        uaWrapper.eq(UserAchievement::getUserId, userId);
        Set<Long> persistedUnlockedIds = userAchievementMapper.selectList(uaWrapper).stream()
                .map(UserAchievement::getAchievementId)
                .collect(java.util.stream.Collectors.toSet());

        // 从 workout_log 表动态计算用户训练指标
        Map<String, Object> stats = computeWorkoutStats(userId);
        int totalWorkouts = (int) stats.getOrDefault("totalWorkouts", 0);
        int streakDays = (int) stats.getOrDefault("streakDays", 0);
        double totalVolume = (double) stats.getOrDefault("totalVolume", 0.0);
        int totalDurationMin = (int) stats.getOrDefault("totalDurationMin", 0);

        // 动态计算每个成就是否应该解锁，并补写缺失的持久化记录
        Set<Long> actualUnlockedIds = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (Achievement a : allAchievements) {
            boolean met = false;
            String type = a.getConditionType() != null ? a.getConditionType() : "";
            int target = a.getConditionValue() != null ? a.getConditionValue() : 0;

            switch (type) {
                case "total_workouts", "workout_count" -> met = totalWorkouts >= target;
                case "streak_days" -> met = streakDays >= target;
                case "total_volume" -> met = totalVolume >= target;
                case "total_duration" -> met = totalDurationMin >= target;
            }

            if (met) {
                actualUnlockedIds.add(a.getId());
                // 补写持久化记录（如果之前未写入）
                if (!persistedUnlockedIds.contains(a.getId())) {
                    UserAchievement ua = new UserAchievement();
                    ua.setUserId(userId);
                    ua.setAchievementId(a.getId());
                    ua.setUnlockedAt(now);
                    try {
                        userAchievementMapper.insert(ua);
                    } catch (Exception e) {
                        // 忽略唯一键冲突（并发场景）
                        log.debug("成就解锁记录已存在，跳过: userId={}, achievementId={}", userId, a.getId());
                    }
                }
            }
        }

        // 按 conditionType 分组
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
                item.put("unlocked", actualUnlockedIds.contains(a.getId()));
                return item;
            }).toList());
            categories.add(category);
        }

        Map<String, Object> statsResult = new HashMap<>();
        statsResult.put("total", allAchievements.size());
        statsResult.put("unlocked", actualUnlockedIds.size());
        statsResult.put("progress", allAchievements.isEmpty() ? 0 :
                Math.round((double) actualUnlockedIds.size() / allAchievements.size() * 100));

        AchievementListResponse response = new AchievementListResponse();
        response.setCategories(categories);
        response.setStats(statsResult);
        return response;
    }

    /**
     * 从 workout_log 表动态计算用户训练指标（通过原生 SQL，避免跨模块依赖）
     */
    private Map<String, Object> computeWorkoutStats(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            java.sql.Connection conn = sqlSession.getConnection();
            java.sql.PreparedStatement ps;
            java.sql.ResultSet rs;

            // 总训练次数
            ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM workout_log WHERE user_id = ? AND status = 'completed'");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            rs.next();
            result.put("totalWorkouts", rs.getInt(1));
            rs.close();
            ps.close();

            // 累计训练量(kg)
            ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(total_volume_kg), 0) FROM workout_log WHERE user_id = ? AND status = 'completed'");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            rs.next();
            result.put("totalVolume", rs.getDouble(1));
            rs.close();
            ps.close();

            // 累计训练时长(分钟)
            ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(duration_min), 0) FROM workout_log WHERE user_id = ? AND status = 'completed'");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            rs.next();
            result.put("totalDurationMin", rs.getInt(1));
            rs.close();
            ps.close();

            // 连续训练天数
            ps = conn.prepareStatement(
                "SELECT DISTINCT workout_date FROM workout_log WHERE user_id = ? AND status = 'completed' ORDER BY workout_date DESC");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            int streak = 0;
            java.time.LocalDate expected = java.time.LocalDate.now();
            while (rs.next()) {
                java.time.LocalDate date = rs.getDate(1).toLocalDate();
                if (date.equals(expected)) {
                    streak++;
                    expected = expected.minusDays(1);
                } else if (streak == 0 && date.isBefore(expected)) {
                    // 今天还没训练，从最近的训练日开始算
                    expected = date;
                    streak++;
                    expected = expected.minusDays(1);
                } else {
                    break;
                }
            }
            result.put("streakDays", streak);
            rs.close();
            ps.close();

        } catch (Exception e) {
            log.warn("计算训练指标失败", e);
            result.putIfAbsent("totalWorkouts", 0);
            result.putIfAbsent("totalVolume", 0.0);
            result.putIfAbsent("totalDurationMin", 0);
            result.putIfAbsent("streakDays", 0);
        }
        return result;
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
