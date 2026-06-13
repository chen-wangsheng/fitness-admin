package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.content.entity.Exercise;
import com.fitness.admin.content.mapper.ExerciseMapper;
import com.fitness.admin.workout.dto.PrRecordItem;
import com.fitness.admin.workout.dto.WorkoutStatsResponse;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.entity.WorkoutLogExercise;
import com.fitness.admin.workout.entity.WorkoutLogSet;
import com.fitness.admin.workout.mapper.WorkoutLogExerciseMapper;
import com.fitness.admin.workout.mapper.WorkoutLogMapper;
import com.fitness.admin.workout.mapper.WorkoutLogSetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练统计与 PR 记录查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppWorkoutStatsService {

    private final WorkoutLogMapper workoutLogMapper;
    private final WorkoutLogExerciseMapper workoutLogExerciseMapper;
    private final WorkoutLogSetMapper workoutLogSetMapper;
    private final ExerciseMapper exerciseMapper;
    private final MiniAppWorkoutAchievementService achievementService;

    public WorkoutStatsResponse getStats(String period) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<WorkoutLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkoutLog::getUserId, userId)
               .eq(WorkoutLog::getStatus, "completed");

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

        int totalWorkouts = logs.size();
        int totalDuration = logs.stream().mapToInt(l -> l.getDurationMin() != null ? l.getDurationMin() : 0).sum();
        BigDecimal totalVolume = logs.stream()
                .map(l -> l.getTotalVolumeKg() != null ? l.getTotalVolumeKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCalories = logs.stream()
                .map(l -> l.getEstimatedCalories() != null ? l.getEstimatedCalories() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int streakDays = achievementService.calculateStreakDays(userId);

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

    public List<PrRecordItem> getPrRecords() {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<WorkoutLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(WorkoutLog::getUserId, userId)
                  .eq(WorkoutLog::getStatus, "completed")
                  .select(WorkoutLog::getId);
        List<WorkoutLog> logs = workoutLogMapper.selectList(logWrapper);
        if (logs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> logIds = logs.stream().map(WorkoutLog::getId).toList();

        LambdaQueryWrapper<WorkoutLogExercise> exWrapper = new LambdaQueryWrapper<>();
        exWrapper.in(WorkoutLogExercise::getWorkoutLogId, logIds)
                 .select(WorkoutLogExercise::getId, WorkoutLogExercise::getExerciseId);
        List<WorkoutLogExercise> exercises = workoutLogExerciseMapper.selectList(exWrapper);
        if (exercises.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Long> logExerciseToExerciseMap = exercises.stream()
                .collect(Collectors.toMap(WorkoutLogExercise::getId, WorkoutLogExercise::getExerciseId));
        List<Long> logExerciseIds = exercises.stream().map(WorkoutLogExercise::getId).toList();

        LambdaQueryWrapper<WorkoutLogSet> setWrapper = new LambdaQueryWrapper<>();
        setWrapper.in(WorkoutLogSet::getWorkoutLogExerciseId, logExerciseIds)
                  .eq(WorkoutLogSet::getCompleted, 1)
                  .isNotNull(WorkoutLogSet::getWeight);
        List<WorkoutLogSet> completedSets = workoutLogSetMapper.selectList(setWrapper);

        Map<Long, WorkoutLogSet> prMap = new HashMap<>();
        for (WorkoutLogSet set : completedSets) {
            Long exerciseId = logExerciseToExerciseMap.get(set.getWorkoutLogExerciseId());
            if (exerciseId == null) continue;
            prMap.merge(exerciseId, set, (existing, current) ->
                    current.getWeight().compareTo(existing.getWeight()) > 0 ? current : existing);
        }

        if (prMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> exerciseIds = new ArrayList<>(prMap.keySet());
        LambdaQueryWrapper<Exercise> exerciseWrapper = new LambdaQueryWrapper<>();
        exerciseWrapper.in(Exercise::getId, exerciseIds)
                       .select(Exercise::getId, Exercise::getName, Exercise::getDemoImageUrl);
        List<Exercise> exerciseList = exerciseMapper.selectList(exerciseWrapper);
        Map<Long, Exercise> exerciseMap = exerciseList.stream()
                .collect(Collectors.toMap(Exercise::getId, e -> e));

        List<PrRecordItem> result = new ArrayList<>();
        for (Map.Entry<Long, WorkoutLogSet> entry : prMap.entrySet()) {
            Exercise ex = exerciseMap.get(entry.getKey());
            if (ex == null) continue;
            WorkoutLogSet set = entry.getValue();

            PrRecordItem item = new PrRecordItem();
            item.setExerciseId(ex.getId());
            item.setExerciseName(ex.getName());
            item.setMaxWeight(set.getWeight());
            item.setReps(set.getReps());
            item.setAchievedDate(set.getCreatedAt() != null ? set.getCreatedAt().toLocalDate() : null);
            item.setDemoImageUrl(ex.getDemoImageUrl());
            result.add(item);
        }

        result.sort((a, b) -> b.getMaxWeight().compareTo(a.getMaxWeight()));
        return result;
    }

    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userId;
    }
}
