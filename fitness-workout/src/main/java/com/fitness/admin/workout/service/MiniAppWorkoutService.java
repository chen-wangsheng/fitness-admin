package com.fitness.admin.workout.service;

import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.workout.dto.AchievementInfo;
import com.fitness.admin.workout.dto.CompleteWorkoutRequest;
import com.fitness.admin.workout.dto.CompleteWorkoutResponse;
import com.fitness.admin.workout.dto.LogSetRequest;
import com.fitness.admin.workout.dto.LogSetResponse;
import com.fitness.admin.workout.dto.PrRecordItem;
import com.fitness.admin.workout.dto.StartWorkoutRequest;
import com.fitness.admin.workout.dto.StartWorkoutResponse;
import com.fitness.admin.workout.dto.WorkoutStatsResponse;
import com.fitness.admin.workout.entity.WorkoutLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小程序训练服务门面,委托给三个内聚的子服务:
 * <ul>
 *   <li>{@link MiniAppWorkoutExecutionService} - 训练开始/记录/完成/历史</li>
 *   <li>{@link MiniAppWorkoutStatsService} - 统计/PR 记录</li>
 *   <li>{@link MiniAppWorkoutAchievementService} - 成就检查与解锁</li>
 * </ul>
 *
 * <p>保留 {@code MiniAppWorkoutService} 名称以兼容 controller。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppWorkoutService {

    private final MiniAppWorkoutExecutionService executionService;
    private final MiniAppWorkoutStatsService statsService;
    private final MiniAppWorkoutAchievementService achievementService;

    public StartWorkoutResponse startWorkout(StartWorkoutRequest request) {
        return executionService.startWorkout(request);
    }

    public LogSetResponse logSet(LogSetRequest request) {
        return executionService.logSet(request);
    }

    public CompleteWorkoutResponse completeWorkout(CompleteWorkoutRequest request) {
        return executionService.completeWorkout(request);
    }

    public PageResult<WorkoutLog> getHistory(Integer pageNum, Integer pageSize,
                                              String startDate, String endDate, String status) {
        return executionService.getHistory(pageNum, pageSize, startDate, endDate, status);
    }

    public WorkoutStatsResponse getStats(String period) {
        return statsService.getStats(period);
    }

    public List<PrRecordItem> getPrRecords() {
        return statsService.getPrRecords();
    }

    public List<AchievementInfo> checkAndUnlockAchievements(Long userId) {
        return achievementService.checkAndUnlockAchievements(userId);
    }
}
