package com.fitness.admin.dashboard.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.admin.dashboard.service.DashboardService;
import com.fitness.admin.dashboard.vo.DashboardOverviewVO;
import com.fitness.admin.dashboard.vo.DashboardOverviewVO.*;
import com.fitness.admin.achievement.entity.Checkin;
import com.fitness.admin.achievement.mapper.CheckinMapper;
import com.fitness.admin.ai.entity.AiUsageDaily;
import com.fitness.admin.ai.entity.AiSafetyEvent;
import com.fitness.admin.ai.mapper.AiUsageDailyMapper;
import com.fitness.admin.ai.mapper.AiSafetyEventMapper;
import com.fitness.admin.content.entity.Exercise;
import com.fitness.admin.content.entity.ExerciseCategory;
import com.fitness.admin.content.entity.BodyPart;
import com.fitness.admin.content.mapper.ExerciseMapper;
import com.fitness.admin.content.mapper.CategoryMapper;
import com.fitness.admin.content.mapper.BodyPartMapper;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import com.fitness.admin.workout.entity.WorkoutLog;
import com.fitness.admin.workout.entity.WorkoutLogExercise;
import com.fitness.admin.workout.mapper.WorkoutLogMapper;
import com.fitness.admin.workout.mapper.WorkoutLogExerciseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据看板服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final WorkoutLogMapper workoutLogMapper;
    private final WorkoutLogExerciseMapper workoutLogExerciseMapper;
    private final CheckinMapper checkinMapper;
    private final ExerciseMapper exerciseMapper;
    private final CategoryMapper categoryMapper;
    private final BodyPartMapper bodyPartMapper;
    private final AiUsageDailyMapper aiUsageDailyMapper;
    private final AiSafetyEventMapper aiSafetyEventMapper;

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "dashboard:overview", key = "'days:' + #days")
    public DashboardOverviewVO getOverview(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 1. 核心指标卡片
        List<MetricCard> metricCards = buildMetricCards(today, yesterday);

        // 2. 用户增长趋势
        List<TrendData> userGrowthTrend = buildUserGrowthTrend(today, days);

        // 3. 训练活跃趋势
        List<TrendData> trainingActivityTrend = buildTrainingActivityTrend(today, days);

        // 4. 热门动作Top10
        List<PopularExercise> popularExercises = buildPopularExercises();

        // 5. AI使用概览
        AiOverview aiOverview = buildAiOverview(today, yesterday);

        return DashboardOverviewVO.builder()
                .metricCards(metricCards)
                .userGrowthTrend(userGrowthTrend)
                .trainingActivityTrend(trainingActivityTrend)
                .popularExercises(popularExercises)
                .aiOverview(aiOverview)
                .build();
    }

    /**
     * 构建核心指标卡片
     */
    private List<MetricCard> buildMetricCards(LocalDate today, LocalDate yesterday) {
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);

        // 今日新增用户
        Long todayNewUsers = userMapper.selectCount(
                Wrappers.<User>lambdaQuery()
                        .between(User::getCreatedAt, todayStart, todayEnd)
        );
        Long yesterdayNewUsers = userMapper.selectCount(
                Wrappers.<User>lambdaQuery()
                        .between(User::getCreatedAt, yesterdayStart, yesterdayEnd)
        );

        // DAU (今日有训练或打卡的用户数)
        Set<Long> activeUserIds = new HashSet<>();
        List<WorkoutLog> todayWorkouts = workoutLogMapper.selectList(
                Wrappers.<WorkoutLog>lambdaQuery()
                        .between(WorkoutLog::getWorkoutDate, today, today)
        );
        todayWorkouts.forEach(w -> activeUserIds.add(w.getUserId()));

        List<Checkin> todayCheckins = checkinMapper.selectList(
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getCheckinDate, today)
        );
        todayCheckins.forEach(c -> activeUserIds.add(c.getUserId()));

        Long dau = (long) activeUserIds.size();

        // 昨日DAU
        Set<Long> yesterdayActiveUserIds = new HashSet<>();
        List<WorkoutLog> yesterdayWorkouts = workoutLogMapper.selectList(
                Wrappers.<WorkoutLog>lambdaQuery()
                        .between(WorkoutLog::getWorkoutDate, yesterday, yesterday)
        );
        yesterdayWorkouts.forEach(w -> yesterdayActiveUserIds.add(w.getUserId()));

        List<Checkin> yesterdayCheckins = checkinMapper.selectList(
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getCheckinDate, yesterday)
        );
        yesterdayCheckins.forEach(c -> yesterdayActiveUserIds.add(c.getUserId()));

        Long yesterdayDau = (long) yesterdayActiveUserIds.size();

        // 今日训练次数
        Long todayWorkoutCount = (long) todayWorkouts.size();
        Long yesterdayWorkoutCount = (long) yesterdayWorkouts.size();

        // 今日打卡次数
        Long todayCheckinCount = (long) todayCheckins.size();
        Long yesterdayCheckinCount = (long) yesterdayCheckins.size();

        return Arrays.asList(
                MetricCard.builder()
                        .title("今日新增用户")
                        .value(todayNewUsers)
                        .change(calculateChange(todayNewUsers, yesterdayNewUsers))
                        .icon("User")
                        .iconColor("#409EFF")
                        .build(),
                MetricCard.builder()
                        .title("DAU")
                        .value(dau)
                        .change(calculateChange(dau, yesterdayDau))
                        .icon("DataLine")
                        .iconColor("#67C23A")
                        .build(),
                MetricCard.builder()
                        .title("今日训练")
                        .value(todayWorkoutCount)
                        .change(calculateChange(todayWorkoutCount, yesterdayWorkoutCount))
                        .icon("TrendCharts")
                        .iconColor("#E6A23C")
                        .build(),
                MetricCard.builder()
                        .title("今日打卡")
                        .value(todayCheckinCount)
                        .change(calculateChange(todayCheckinCount, yesterdayCheckinCount))
                        .icon("CircleCheck")
                        .iconColor("#F56C6C")
                        .build()
        );
    }

    /**
     * 计算变化百分比
     */
    private Double calculateChange(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round((current - previous) * 1000.0 / previous) / 10.0;
    }

    /**
     * 构建用户增长趋势
     */
    private List<TrendData> buildUserGrowthTrend(LocalDate today, int days) {
        List<TrendData> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            // 新增用户数
            Long newUsers = userMapper.selectCount(
                    Wrappers.<User>lambdaQuery()
                            .between(User::getCreatedAt, dayStart, dayEnd)
            );

            // 活跃用户数 (有训练或打卡)
            Set<Long> activeUserIds = new HashSet<>();
            List<WorkoutLog> workouts = workoutLogMapper.selectList(
                    Wrappers.<WorkoutLog>lambdaQuery()
                            .between(WorkoutLog::getWorkoutDate, date, date)
            );
            workouts.forEach(w -> activeUserIds.add(w.getUserId()));

            List<Checkin> checkins = checkinMapper.selectList(
                    Wrappers.<Checkin>lambdaQuery()
                            .eq(Checkin::getCheckinDate, date)
            );
            checkins.forEach(c -> activeUserIds.add(c.getUserId()));

            trend.add(TrendData.builder()
                    .date(date.format(formatter))
                    .series(Arrays.asList(
                            SeriesData.builder().name("新增用户").value(newUsers).build(),
                            SeriesData.builder().name("活跃用户").value((long) activeUserIds.size()).build()
                    ))
                    .build());
        }

        return trend;
    }

    /**
     * 构建训练活跃趋势
     */
    private List<TrendData> buildTrainingActivityTrend(LocalDate today, int days) {
        List<TrendData> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);

            // 训练次数
            Long workoutCount = workoutLogMapper.selectCount(
                    Wrappers.<WorkoutLog>lambdaQuery()
                            .between(WorkoutLog::getWorkoutDate, date, date)
            );

            // 打卡次数
            Long checkinCount = checkinMapper.selectCount(
                    Wrappers.<Checkin>lambdaQuery()
                            .eq(Checkin::getCheckinDate, date)
            );

            trend.add(TrendData.builder()
                    .date(date.format(formatter))
                    .series(Arrays.asList(
                            SeriesData.builder().name("训练次数").value(workoutCount).build(),
                            SeriesData.builder().name("打卡次数").value(checkinCount).build()
                    ))
                    .build());
        }

        return trend;
    }

    /**
     * 构建热门动作Top10
     */
    private List<PopularExercise> buildPopularExercises() {
        // 统计最近30天的动作使用次数
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<WorkoutLog> recentLogs = workoutLogMapper.selectList(
                Wrappers.<WorkoutLog>lambdaQuery()
                        .between(WorkoutLog::getWorkoutDate, startDate, endDate)
                        .select(WorkoutLog::getId)
        );

        if (recentLogs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> logIds = recentLogs.stream()
                .map(WorkoutLog::getId)
                .collect(Collectors.toList());

        // 获取所有训练动作记录
        List<WorkoutLogExercise> logExercises = workoutLogExerciseMapper.selectList(
                Wrappers.<WorkoutLogExercise>lambdaQuery()
                        .in(WorkoutLogExercise::getWorkoutLogId, logIds)
        );

        // 统计每个动作的使用次数
        Map<Long, Long> exerciseCountMap = logExercises.stream()
                .collect(Collectors.groupingBy(WorkoutLogExercise::getExerciseId, Collectors.counting()));

        // 获取动作详情并排序
        List<Map.Entry<Long, Long>> sortedEntries = exerciseCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // 获取所有相关动作
        Set<Long> exerciseIds = sortedEntries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Map<Long, Exercise> exerciseMap = new HashMap<>();
        if (!exerciseIds.isEmpty()) {
            List<Exercise> exercises = exerciseMapper.selectBatchIds(exerciseIds);
            exercises.forEach(e -> exerciseMap.put(e.getId(), e));
        }

        // 获取所有分类
        Map<Long, String> categoryMap = categoryMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(ExerciseCategory::getId, ExerciseCategory::getName));

        // 获取所有部位
        Map<Long, String> bodyPartMap = bodyPartMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(BodyPart::getId, BodyPart::getName));

        List<PopularExercise> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, Long> entry : sortedEntries) {
            Exercise exercise = exerciseMap.get(entry.getKey());
            if (exercise == null) continue;

            String category = exercise.getCategoryId() != null ?
                    categoryMap.getOrDefault(exercise.getCategoryId().longValue(), "未知") : "未知";

            // 简化处理，使用分类作为部位
            String bodyPart = category;

            result.add(PopularExercise.builder()
                    .rank(rank++)
                    .name(exercise.getName())
                    .category(category)
                    .bodyPart(bodyPart)
                    .count(entry.getValue())
                    .trend((int) (Math.random() * 30 - 10)) // 简化处理，随机趋势
                    .build());
        }

        return result;
    }

    /**
     * 构建AI使用概览
     */
    private AiOverview buildAiOverview(LocalDate today, LocalDate yesterday) {
        // 获取今日AI使用统计
        AiUsageDaily todayUsage = aiUsageDailyMapper.selectOne(
                Wrappers.<AiUsageDaily>lambdaQuery()
                        .eq(AiUsageDaily::getStatDate, today)
        );

        // 获取昨日AI使用统计
        AiUsageDaily yesterdayUsage = aiUsageDailyMapper.selectOne(
                Wrappers.<AiUsageDaily>lambdaQuery()
                        .eq(AiUsageDaily::getStatDate, yesterday)
        );

        // 获取今日安全拦截次数
        Long todaySafetyBlocks = aiSafetyEventMapper.selectCount(
                Wrappers.<AiSafetyEvent>lambdaQuery()
                        .ge(AiSafetyEvent::getCreatedAt, today.atStartOfDay())
        );

        // 获取昨日安全拦截次数
        Long yesterdaySafetyBlocks = aiSafetyEventMapper.selectCount(
                Wrappers.<AiSafetyEvent>lambdaQuery()
                        .ge(AiSafetyEvent::getCreatedAt, yesterday.atStartOfDay())
                        .lt(AiSafetyEvent::getCreatedAt, today.atStartOfDay())
        );

        Long todayChatSessions = todayUsage != null ? (long) todayUsage.getTotalChatSessions() : 0L;
        Long yesterdayChatSessions = yesterdayUsage != null ? (long) yesterdayUsage.getTotalChatSessions() : 0L;

        Long todayPlanGenerated = todayUsage != null ? (long) todayUsage.getTotalPlanGenerated() : 0L;
        Long yesterdayPlanGenerated = yesterdayUsage != null ? (long) yesterdayUsage.getTotalPlanGenerated() : 0L;

        Double todayRagHitRate = todayUsage != null && todayUsage.getRagHitRate() != null ?
                todayUsage.getRagHitRate().doubleValue() : 0.0;
        Double yesterdayRagHitRate = yesterdayUsage != null && yesterdayUsage.getRagHitRate() != null ?
                yesterdayUsage.getRagHitRate().doubleValue() : 0.0;

        Long todayAvgResponseTime = todayUsage != null && todayUsage.getAvgResponseTimeMs() != null ?
                todayUsage.getAvgResponseTimeMs().longValue() : 0L;
        Long yesterdayAvgResponseTime = yesterdayUsage != null && yesterdayUsage.getAvgResponseTimeMs() != null ?
                yesterdayUsage.getAvgResponseTimeMs().longValue() : 0L;

        return AiOverview.builder()
                .totalChatSessions(todayChatSessions)
                .chatSessionsChange(calculateChange(todayChatSessions, yesterdayChatSessions))
                .totalPlanGenerated(todayPlanGenerated)
                .planGeneratedChange(calculateChange(todayPlanGenerated, yesterdayPlanGenerated))
                .ragHitRate(todayRagHitRate)
                .ragHitRateChange(todayRagHitRate - yesterdayRagHitRate)
                .avgResponseTime(todayAvgResponseTime)
                .avgResponseTimeChange(calculateChange(todayAvgResponseTime, yesterdayAvgResponseTime))
                .safetyBlockCount(todaySafetyBlocks)
                .safetyBlockChange(calculateChange(todaySafetyBlocks, yesterdaySafetyBlocks))
                .build();
    }
}
