package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.achievement.entity.Achievement;
import com.fitness.admin.achievement.entity.UserAchievement;
import com.fitness.admin.achievement.mapper.AchievementMapper;
import com.fitness.admin.achievement.mapper.UserAchievementMapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import com.fitness.admin.workout.dto.*;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.entity.WorkoutLogExercise;
import com.fitness.admin.workout.entity.WorkoutLogSet;
import com.fitness.admin.workout.mapper.WorkoutLogExerciseMapper;
import com.fitness.admin.workout.mapper.WorkoutLogMapper;
import com.fitness.admin.workout.mapper.WorkoutLogSetMapper;
import com.fitness.admin.content.mapper.PlanDayExerciseMapper;
import com.fitness.admin.content.vo.PlanExerciseVO;
import com.fitness.admin.content.entity.Exercise;
import com.fitness.admin.content.mapper.ExerciseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 小程序训练服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppWorkoutService {

    private final WorkoutLogMapper workoutLogMapper;
    private final WorkoutLogExerciseMapper workoutLogExerciseMapper;
    private final WorkoutLogSetMapper workoutLogSetMapper;
    private final UserMapper userMapper;
    private final PlanDayExerciseMapper planDayExerciseMapper;
    private final ExerciseMapper exerciseMapper;
    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;

    /**
     * 开始训练
     */
    @Transactional
    public StartWorkoutResponse startWorkout(StartWorkoutRequest request) {
        Long userId = getCurrentUserId();

        // 如果指定了计划，更新用户的当前活跃计划
        if (request.getPlanId() != null) {
            User user = userMapper.selectById(userId);
            if (user != null && !request.getPlanId().equals(user.getCurrentPlanId())) {
                user.setCurrentPlanId(request.getPlanId());
                userMapper.updateById(user);
            }
        }

        // 创建训练记录
        WorkoutLog workoutLog = new WorkoutLog();
        workoutLog.setUserId(userId);
        workoutLog.setPlanId(request.getPlanId());
        workoutLog.setPlanDayId(request.getPlanDayId());
        workoutLog.setWorkoutDate(LocalDate.now());
        workoutLog.setStartTime(LocalDateTime.now());
        workoutLog.setStatus("in_progress");
        workoutLog.setTotalVolumeKg(BigDecimal.ZERO);
        workoutLog.setTotalSets(0);
        workoutLog.setEstimatedCalories(BigDecimal.ZERO);
        workoutLog.setCreatedAt(LocalDateTime.now());
        workoutLog.setUpdatedAt(LocalDateTime.now());
        workoutLogMapper.insert(workoutLog);

        // 根据计划创建训练动作记录
        List<ExerciseInfo> exercises = new ArrayList<>();
        if (request.getPlanDayId() != null) {
            List<PlanExerciseVO> planExercises = planDayExerciseMapper.selectByPlanDayId(request.getPlanDayId());
            for (PlanExerciseVO pe : planExercises) {
                // 跳过 exercise_id 无效的记录（动作已被删除）
                if (pe.getExerciseId() == null || pe.getExerciseName() == null) {
                    log.warn("计划动作 exercise_id={} 在 exercise 表中不存在，已跳过", pe.getExerciseId());
                    continue;
                }
                WorkoutLogExercise logExercise = new WorkoutLogExercise();
                logExercise.setWorkoutLogId(workoutLog.getId());
                logExercise.setExerciseId(pe.getExerciseId());
                logExercise.setSortOrder(pe.getSort() != null ? pe.getSort() : 0);
                workoutLogExerciseMapper.insert(logExercise);

                ExerciseInfo info = new ExerciseInfo();
                info.setLogExerciseId(logExercise.getId());
                info.setExerciseId(pe.getExerciseId());
                info.setExerciseName(pe.getExerciseName());
                info.setSets(pe.getSets() != null ? pe.getSets() : 3);
                info.setReps(parseReps(pe.getReps()));
                info.setRestSeconds(pe.getRestSeconds() != null ? pe.getRestSeconds() : 60);
                info.setSort(pe.getSort());
                exercises.add(info);
            }
        }

        StartWorkoutResponse response = new StartWorkoutResponse();
        response.setWorkoutLogId(workoutLog.getId());
        response.setStartTime(workoutLog.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setExercises(exercises);

        return response;
    }

    /**
     * 记录一组数据
     */
    @Transactional
    public LogSetResponse logSet(LogSetRequest request) {
        Long userId = getCurrentUserId();

        // 保存训练组数据
        WorkoutLogSet logSet = new WorkoutLogSet();
        logSet.setWorkoutLogExerciseId(request.getLogExerciseId());
        logSet.setSetNumber(request.getSetNumber());
        logSet.setReps(request.getReps());
        logSet.setWeight(request.getWeightKg());
        logSet.setCompleted(Boolean.TRUE.equals(request.getIsCompleted()) ? 1 : 0);
        logSet.setCreatedAt(LocalDateTime.now());
        workoutLogSetMapper.insert(logSet);

        // TODO: 检查是否是PR记录
        boolean isPr = false;

        LogSetResponse response = new LogSetResponse();
        response.setLogSetId(logSet.getId());
        response.setIsPr(isPr);

        return response;
    }

    /**
     * 完成训练
     */
    @Transactional
    public CompleteWorkoutResponse completeWorkout(CompleteWorkoutRequest request) {
        Long userId = getCurrentUserId();

        // TODO: 获取当前进行中的训练记录
        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutLog::getUserId, userId)
               .eq(WorkoutLog::getStatus, "in_progress")
               .orderByDesc(WorkoutLog::getStartTime)
               .last("LIMIT 1");
        WorkoutLog workoutLog = workoutLogMapper.selectOne(wrapper);

        if (workoutLog == null) {
            throw new BizException("没有进行中的训练");
        }

        // 更新训练记录
        workoutLog.setEndTime(LocalDateTime.now());
        long durationSeconds = ChronoUnit.SECONDS.between(workoutLog.getStartTime(), workoutLog.getEndTime());
        workoutLog.setDurationMin((int) Math.max(1, durationSeconds)); // 存储秒数（与SQL注释一致）
        workoutLog.setFeelingScore(request.getFeelingScore());
        workoutLog.setRpe(request.getRpe());
        workoutLog.setNotes(request.getNotes());
        workoutLog.setStatus("completed");
        workoutLog.setUpdatedAt(LocalDateTime.now());

        // 从 workout_log_exercise + workout_log_set 聚合统计
        LambdaQueryWrapper<WorkoutLogExercise> exWrapper = new LambdaQueryWrapper<>();
        exWrapper.eq(WorkoutLogExercise::getWorkoutLogId, workoutLog.getId())
                 .select(WorkoutLogExercise::getId);
        List<WorkoutLogExercise> logExercises = workoutLogExerciseMapper.selectList(exWrapper);

        int completedSets = 0;
        BigDecimal totalVolume = BigDecimal.ZERO;

        if (!logExercises.isEmpty()) {
            List<Long> logExerciseIds = logExercises.stream()
                    .map(WorkoutLogExercise::getId)
                    .toList();

            LambdaQueryWrapper<WorkoutLogSet> setWrapper = new LambdaQueryWrapper<>();
            setWrapper.in(WorkoutLogSet::getWorkoutLogExerciseId, logExerciseIds)
                      .eq(WorkoutLogSet::getCompleted, 1);
            List<WorkoutLogSet> completedSetList = workoutLogSetMapper.selectList(setWrapper);

            completedSets = completedSetList.size();
            for (WorkoutLogSet set : completedSetList) {
                BigDecimal w = set.getWeight() != null ? set.getWeight() : BigDecimal.ZERO;
                int r = set.getReps() != null ? set.getReps() : 0;
                totalVolume = totalVolume.add(w.multiply(BigDecimal.valueOf(r)));
            }
        }

        // 简易卡路里估算：时长(分钟) × 5（中等强度平均消耗）
        double durationMinutes = workoutLog.getDurationMin() / 60.0;
        BigDecimal estimatedCalories = BigDecimal.valueOf(durationMinutes)
                .multiply(BigDecimal.valueOf(5))
                .setScale(0, java.math.RoundingMode.HALF_UP);

        workoutLog.setTotalVolumeKg(totalVolume);
        workoutLog.setTotalSets(completedSets);
        workoutLog.setEstimatedCalories(estimatedCalories);

        workoutLogMapper.updateById(workoutLog);

        // 检查并解锁成就
        List<AchievementInfo> achievements = checkAndUnlockAchievements(userId);

        CompleteWorkoutResponse response = new CompleteWorkoutResponse();
        response.setWorkoutLogId(workoutLog.getId());
        response.setDurationMin(workoutLog.getDurationMin());
        response.setTotalVolumeKg(workoutLog.getTotalVolumeKg());
        response.setTotalSets(workoutLog.getTotalSets());
        response.setEstimatedCalories(workoutLog.getEstimatedCalories());
        response.setAchievements(achievements);

        return response;
    }

    /**
     * 获取训练历史
     */
    public PageResult<WorkoutLog> getHistory(Integer pageNum, Integer pageSize,
                                              String startDate, String endDate, String status) {
        Long userId = getCurrentUserId();

        Page<WorkoutLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutLog::getUserId, userId);

        if (startDate != null) {
            wrapper.ge(WorkoutLog::getWorkoutDate, LocalDate.parse(startDate));
        }
        if (endDate != null) {
            wrapper.le(WorkoutLog::getWorkoutDate, LocalDate.parse(endDate));
        }
        if (status != null) {
            wrapper.eq(WorkoutLog::getStatus, status);
        }

        wrapper.orderByDesc(WorkoutLog::getWorkoutDate);
        Page<WorkoutLog> result = workoutLogMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    /**
     * 获取训练统计
     */
    public WorkoutStatsResponse getStats(String period) {
        Long userId = getCurrentUserId();

        // 查询训练记录
        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutLog::getUserId, userId)
               .eq(WorkoutLog::getStatus, "completed");

        // 计算日期范围（all不筛选时间）
        LocalDate endDate = LocalDate.now();
        if (!"all".equals(period)) {
            LocalDate startDate;
            switch (period) {
                case "month":
                    startDate = endDate.withDayOfMonth(1);
                    break;
                case "year":
                    startDate = endDate.withDayOfYear(1);
                    break;
                case "week":
                default:
                    startDate = endDate.minusDays(endDate.getDayOfWeek().getValue() - 1);
                    break;
            }
            wrapper.ge(WorkoutLog::getWorkoutDate, startDate);
        }
        wrapper.le(WorkoutLog::getWorkoutDate, endDate);
        List<WorkoutLog> logs = workoutLogMapper.selectList(wrapper);

        // 统计数据
        int totalWorkouts = logs.size();
        int totalDuration = logs.stream().mapToInt(l -> l.getDurationMin() != null ? l.getDurationMin() : 0).sum();
        BigDecimal totalVolume = logs.stream()
                .map(l -> l.getTotalVolumeKg() != null ? l.getTotalVolumeKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCalories = logs.stream()
                .map(l -> l.getEstimatedCalories() != null ? l.getEstimatedCalories() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算连续打卡天数
        int streakDays = calculateStreakDays(userId);

        WorkoutStatsResponse response = new WorkoutStatsResponse();
        response.setTotalWorkouts(totalWorkouts);
        response.setTotalDurationMin(totalDuration);
        response.setTotalVolumeKg(totalVolume);
        response.setTotalCalories(totalCalories);
        response.setAvgDurationMin(totalWorkouts > 0 ? totalDuration / totalWorkouts : 0);
        response.setStreakDays(streakDays);
        response.setWorkoutDays(new ArrayList<>());
        response.setPrRecords(new ArrayList<>());

        return response;
    }

    /**
     * 获取个人最佳记录列表
     */
    public List<PrRecordItem> getPrRecords() {
        Long userId = getCurrentUserId();

        // 1. 查询用户所有已完成的训练记录ID
        LambdaQueryWrapper<WorkoutLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(WorkoutLog::getUserId, userId)
                  .eq(WorkoutLog::getStatus, "completed")
                  .select(WorkoutLog::getId);
        List<WorkoutLog> logs = workoutLogMapper.selectList(logWrapper);
        if (logs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> logIds = logs.stream().map(WorkoutLog::getId).toList();

        // 2. 查询这些训练记录的所有动作记录
        LambdaQueryWrapper<WorkoutLogExercise> exWrapper = new LambdaQueryWrapper<>();
        exWrapper.in(WorkoutLogExercise::getWorkoutLogId, logIds)
                 .select(WorkoutLogExercise::getId, WorkoutLogExercise::getExerciseId);
        List<WorkoutLogExercise> exercises = workoutLogExerciseMapper.selectList(exWrapper);
        if (exercises.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Long> logExerciseToExerciseMap = exercises.stream()
                .collect(java.util.stream.Collectors.toMap(WorkoutLogExercise::getId, WorkoutLogExercise::getExerciseId));
        List<Long> logExerciseIds = exercises.stream().map(WorkoutLogExercise::getId).toList();

        // 3. 查询所有已完成的组（不依赖 is_pr 标记，直接从全量数据中取最大重量）
        LambdaQueryWrapper<WorkoutLogSet> setWrapper = new LambdaQueryWrapper<>();
        setWrapper.in(WorkoutLogSet::getWorkoutLogExerciseId, logExerciseIds)
                  .eq(WorkoutLogSet::getCompleted, 1)
                  .isNotNull(WorkoutLogSet::getWeight);
        List<WorkoutLogSet> completedSets = workoutLogSetMapper.selectList(setWrapper);

        // 4. 按动作分组，取最大重量
        Map<Long, WorkoutLogSet> prMap = new java.util.HashMap<>();
        for (WorkoutLogSet set : completedSets) {
            Long exerciseId = logExerciseToExerciseMap.get(set.getWorkoutLogExerciseId());
            if (exerciseId == null) continue;
            prMap.merge(exerciseId, set, (existing, current) ->
                    current.getWeight().compareTo(existing.getWeight()) > 0 ? current : existing);
        }

        if (prMap.isEmpty()) {
            return new ArrayList<>();
        }

        // 5. 批量查询动作信息（名称、演示图）
        List<Long> exerciseIds = new ArrayList<>(prMap.keySet());
        LambdaQueryWrapper<Exercise> exerciseWrapper = new LambdaQueryWrapper<>();
        exerciseWrapper.in(Exercise::getId, exerciseIds)
                       .select(Exercise::getId, Exercise::getName, Exercise::getDemoImageUrl);
        List<Exercise> exerciseList = exerciseMapper.selectList(exerciseWrapper);
        Map<Long, Exercise> exerciseMap = exerciseList.stream()
                .collect(java.util.stream.Collectors.toMap(Exercise::getId, e -> e));

        // 6. 构建响应
        List<PrRecordItem> result = new ArrayList<>();
        for (Map.Entry<Long, WorkoutLogSet> entry : prMap.entrySet()) {
            Long exerciseId = entry.getKey();
            WorkoutLogSet bestSet = entry.getValue();
            Exercise exercise = exerciseMap.get(exerciseId);

            PrRecordItem item = new PrRecordItem();
            item.setExerciseId(exerciseId);
            item.setExerciseName(exercise != null ? exercise.getName() : "动作#" + exerciseId);
            item.setDemoImageUrl(exercise != null ? exercise.getDemoImageUrl() : null);
            item.setMaxWeight(bestSet.getWeight());
            item.setReps(bestSet.getReps());
            item.setAchievedDate(bestSet.getCreatedAt() != null ?
                    bestSet.getCreatedAt().toLocalDate() : null);
            result.add(item);
        }

        // 按最大重量降序排列
        result.sort((a, b) -> b.getMaxWeight().compareTo(a.getMaxWeight()));

        return result;
    }

    /**
     * 检查并解锁成就 — 根据用户当前训练数据判断是否满足成就条件
     */
    @Transactional
    public List<AchievementInfo> checkAndUnlockAchievements(Long userId) {
        // 查询所有成就定义
        List<Achievement> allAchievements = achievementMapper.selectList(null);
        if (allAchievements.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户已解锁的成就ID
        LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
        uaWrapper.eq(UserAchievement::getUserId, userId);
        Set<Long> unlockedIds = userAchievementMapper.selectList(uaWrapper).stream()
                .map(UserAchievement::getAchievementId)
                .collect(java.util.stream.Collectors.toSet());

        // 查询用户所有已完成的训练记录
        LambdaQueryWrapper<WorkoutLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(WorkoutLog::getUserId, userId)
                  .eq(WorkoutLog::getStatus, "completed");
        List<WorkoutLog> logs = workoutLogMapper.selectList(logWrapper);

        // 计算各维度指标
        int totalWorkouts = logs.size();
        int streakDays = calculateStreakDays(userId);
        BigDecimal totalVolume = logs.stream()
                .map(l -> l.getTotalVolumeKg() != null ? l.getTotalVolumeKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalDurationMin = logs.stream()
                .mapToInt(l -> l.getDurationMin() != null ? l.getDurationMin() : 0)
                .sum();

        // 逐个成就检查，收集新解锁的成就
        List<AchievementInfo> newlyUnlocked = new ArrayList<>();
        List<UserAchievement> toInsert = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

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

        // 批量插入解锁记录
        for (UserAchievement ua : toInsert) {
            userAchievementMapper.insert(ua);
        }

        return newlyUnlocked;
    }

    /**
     * 计算连续打卡天数
     */
    private int calculateStreakDays(Long userId) {
        // 查询用户所有已完成训练的日期（去重，倒序）
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
                // 如果今天还没有训练记录，从昨天开始算
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

    /**
     * 解析 reps 字符串为整数（支持 "10-12" 范围格式）
     */
    private Integer parseReps(String reps) {
        if (reps == null || reps.isBlank()) return 12;
        try {
            // 处理范围格式 "10-12"，取第一个值
            if (reps.contains("-")) {
                return Integer.parseInt(reps.split("-")[0].trim());
            }
            return Integer.parseInt(reps.trim());
        } catch (NumberFormatException e) {
            return 12;
        }
    }
}
