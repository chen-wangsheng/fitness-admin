package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

/**
 * 小程序训练执行服务:开始训练、记录组、完成训练、历史查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppWorkoutExecutionService {

    private final WorkoutLogMapper workoutLogMapper;
    private final WorkoutLogExerciseMapper workoutLogExerciseMapper;
    private final WorkoutLogSetMapper workoutLogSetMapper;
    private final UserMapper userMapper;
    private final PlanDayExerciseMapper planDayExerciseMapper;
    private final MiniAppWorkoutAchievementService achievementService;

    @Transactional
    public StartWorkoutResponse startWorkout(StartWorkoutRequest request) {
        Long userId = getCurrentUserId();

        if (request.getPlanId() != null) {
            User user = userMapper.selectById(userId);
            if (user != null && !request.getPlanId().equals(user.getCurrentPlanId())) {
                user.setCurrentPlanId(request.getPlanId());
                userMapper.updateById(user);
            }
        }

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

        List<ExerciseInfo> exercises = new ArrayList<>();
        if (request.getPlanDayId() != null) {
            List<PlanExerciseVO> planExercises = planDayExerciseMapper.selectByPlanDayId(request.getPlanDayId());
            for (PlanExerciseVO pe : planExercises) {
                if (pe.getExerciseId() == null || pe.getExerciseName() == null) {
                    log.warn("计划动作 exercise_id={} 在 exercise 表中不存在,已跳过", pe.getExerciseId());
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

    @Transactional
    public LogSetResponse logSet(LogSetRequest request) {
        Long userId = getCurrentUserId();

        WorkoutLogSet logSet = new WorkoutLogSet();
        logSet.setWorkoutLogExerciseId(request.getLogExerciseId());
        logSet.setSetNumber(request.getSetNumber());
        logSet.setReps(request.getReps());
        logSet.setWeight(request.getWeightKg());
        logSet.setCompleted(Boolean.TRUE.equals(request.getIsCompleted()) ? 1 : 0);
        logSet.setCreatedAt(LocalDateTime.now());
        workoutLogSetMapper.insert(logSet);

        // TODO: PR 检测
        boolean isPr = false;

        LogSetResponse response = new LogSetResponse();
        response.setLogSetId(logSet.getId());
        response.setIsPr(isPr);
        return response;
    }

    @Transactional
    public CompleteWorkoutResponse completeWorkout(CompleteWorkoutRequest request) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutLog::getUserId, userId)
               .eq(WorkoutLog::getStatus, "in_progress")
               .orderByDesc(WorkoutLog::getStartTime)
               .last("LIMIT 1");
        WorkoutLog workoutLog = workoutLogMapper.selectOne(wrapper);
        if (workoutLog == null) {
            throw new BizException("没有进行中的训练");
        }

        workoutLog.setEndTime(LocalDateTime.now());
        long durationSeconds = ChronoUnit.SECONDS.between(workoutLog.getStartTime(), workoutLog.getEndTime());
        workoutLog.setDurationMin((int) Math.max(1, durationSeconds));
        workoutLog.setFeelingScore(request.getFeelingScore());
        workoutLog.setRpe(request.getRpe());
        workoutLog.setNotes(request.getNotes());
        workoutLog.setStatus("completed");
        workoutLog.setUpdatedAt(LocalDateTime.now());

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

        double durationMinutes = workoutLog.getDurationMin() / 60.0;
        BigDecimal estimatedCalories = BigDecimal.valueOf(durationMinutes)
                .multiply(BigDecimal.valueOf(5))
                .setScale(0, java.math.RoundingMode.HALF_UP);

        workoutLog.setTotalVolumeKg(totalVolume);
        workoutLog.setTotalSets(completedSets);
        workoutLog.setEstimatedCalories(estimatedCalories);
        workoutLogMapper.updateById(workoutLog);

        // 委托给成就服务做解锁检测
        List<AchievementInfo> achievements = achievementService.checkAndUnlockAchievements(userId);

        CompleteWorkoutResponse response = new CompleteWorkoutResponse();
        response.setWorkoutLogId(workoutLog.getId());
        response.setDurationMin(workoutLog.getDurationMin());
        response.setTotalVolumeKg(workoutLog.getTotalVolumeKg());
        response.setTotalSets(workoutLog.getTotalSets());
        response.setEstimatedCalories(workoutLog.getEstimatedCalories());
        response.setAchievements(achievements);
        return response;
    }

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

    private Integer parseReps(String reps) {
        if (reps == null || reps.isBlank()) return 12;
        try {
            if (reps.contains("-")) {
                return Integer.parseInt(reps.split("-")[0].trim());
            }
            return Integer.parseInt(reps.trim());
        } catch (NumberFormatException e) {
            return 12;
        }
    }

    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userId;
    }
}
