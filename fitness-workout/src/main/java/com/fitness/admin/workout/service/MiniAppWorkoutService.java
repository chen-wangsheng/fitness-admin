package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.workout.dto.*;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.entity.WorkoutLogExercise;
import com.fitness.admin.workout.entity.WorkoutLogSet;
import com.fitness.admin.workout.mapper.WorkoutLogExerciseMapper;
import com.fitness.admin.workout.mapper.WorkoutLogMapper;
import com.fitness.admin.workout.mapper.WorkoutLogSetMapper;
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

    /**
     * 开始训练
     */
    @Transactional
    public StartWorkoutResponse startWorkout(StartWorkoutRequest request) {
        Long userId = getCurrentUserId();

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

        // TODO: 根据planId和planDayId查询计划动作列表
        // 这里返回空列表，实际需要从plan_day_exercise表查询
        List<ExerciseInfo> exercises = new ArrayList<>();

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
        workoutLog.setDurationMin((int) ChronoUnit.MINUTES.between(workoutLog.getStartTime(), workoutLog.getEndTime()));
        workoutLog.setFeelingScore(request.getFeelingScore());
        workoutLog.setRpe(request.getRpe());
        workoutLog.setNotes(request.getNotes());
        workoutLog.setStatus("completed");
        workoutLog.setUpdatedAt(LocalDateTime.now());

        // TODO: 计算总训练量和卡路里
        workoutLog.setTotalVolumeKg(BigDecimal.ZERO);
        workoutLog.setTotalSets(0);
        workoutLog.setEstimatedCalories(BigDecimal.ZERO);

        workoutLogMapper.updateById(workoutLog);

        // TODO: 检查并解锁成就
        List<AchievementInfo> achievements = new ArrayList<>();

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

        // 计算日期范围
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();
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

        // 查询训练记录
        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutLog::getUserId, userId)
               .eq(WorkoutLog::getStatus, "completed")
               .ge(WorkoutLog::getWorkoutDate, startDate)
               .le(WorkoutLog::getWorkoutDate, endDate);
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
     * 计算连续打卡天数
     */
    private int calculateStreakDays(Long userId) {
        // TODO: 实现连续打卡天数计算
        return 0;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }
        return userId;
    }
}
