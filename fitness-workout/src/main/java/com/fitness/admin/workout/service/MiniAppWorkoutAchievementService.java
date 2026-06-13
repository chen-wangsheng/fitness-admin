package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.achievement.entity.Achievement;
import com.fitness.admin.achievement.entity.UserAchievement;
import com.fitness.admin.achievement.mapper.AchievementMapper;
import com.fitness.admin.achievement.mapper.UserAchievementMapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.workout.dto.AchievementInfo;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.mapper.WorkoutLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 训练完成后,根据用户累计数据检查成就条件并解锁。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppWorkoutAchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final WorkoutLogMapper workoutLogMapper;

    @Transactional
    public List<AchievementInfo> checkAndUnlockAchievements(Long userId) {
        List<Achievement> allAchievements = achievementMapper.selectList(null);
        if (allAchievements.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
        uaWrapper.eq(UserAchievement::getUserId, userId);
        Set<Long> unlockedIds = userAchievementMapper.selectList(uaWrapper).stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<WorkoutLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(WorkoutLog::getUserId, userId)
                  .eq(WorkoutLog::getStatus, "completed");
        List<WorkoutLog> logs = workoutLogMapper.selectList(logWrapper);

        int totalWorkouts = logs.size();
        int streakDays = calculateStreakDays(userId);
        BigDecimal totalVolume = logs.stream()
                .map(l -> l.getTotalVolumeKg() != null ? l.getTotalVolumeKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalDurationMin = logs.stream()
                .mapToInt(l -> l.getDurationMin() != null ? l.getDurationMin() : 0)
                .sum();

        List<AchievementInfo> newlyUnlocked = new ArrayList<>();
        List<UserAchievement> toInsert = new ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (Achievement a : allAchievements) {
            if (unlockedIds.contains(a.getId())) continue;

            boolean met = switch (a.getConditionType()) {
                case "total_workouts", "workout_count" -> totalWorkouts >= a.getConditionValue();
                case "streak_days" -> streakDays >= a.getConditionValue();
                case "total_volume" -> totalVolume.compareTo(BigDecimal.valueOf(a.getConditionValue())) >= 0;
                case "total_duration" -> totalDurationMin >= a.getConditionValue();
                default -> false;
            };

            if (met) {
                UserAchievement ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementId(a.getId());
                ua.setUnlockedAt(now);
                toInsert.add(ua);

                AchievementInfo info = new AchievementInfo();
                info.setId(a.getId());
                info.setName(a.getName());
                info.setDescription(a.getDescription());
                info.setIconUrl(a.getIconUrl());
                newlyUnlocked.add(info);

                log.info("用户 {} 解锁成就: {} ({})", userId, a.getName(), a.getConditionType());
            }
        }

        for (UserAchievement ua : toInsert) {
            userAchievementMapper.insert(ua);
        }
        return newlyUnlocked;
    }

    int calculateStreakDays(Long userId) {
        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutLog::getUserId, userId)
               .eq(WorkoutLog::getStatus, "completed")
               .select(WorkoutLog::getWorkoutDate)
               .groupBy(WorkoutLog::getWorkoutDate)
               .orderByDesc(WorkoutLog::getWorkoutDate);
        List<WorkoutLog> logs = workoutLogMapper.selectList(wrapper);

        if (logs.isEmpty()) {
            return 0;
        }

        int streak = 0;
        LocalDate expected = LocalDate.now();

        for (WorkoutLog log : logs) {
            LocalDate workoutDate = log.getWorkoutDate();
            if (workoutDate.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (workoutDate.isBefore(expected)) {
                if (streak == 0 && expected.equals(LocalDate.now())) {
                    expected = workoutDate.plusDays(1);
                    if (workoutDate.equals(expected.minusDays(1))) {
                        streak++;
                        expected = expected.minusDays(1);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return streak;
    }

    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userId;
    }
}
