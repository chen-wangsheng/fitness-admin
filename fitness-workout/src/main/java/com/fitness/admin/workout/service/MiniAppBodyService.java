package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import com.fitness.admin.workout.dto.*;
import com.fitness.admin.workout.entity.BodyMetric;
import com.fitness.admin.workout.mapper.BodyMetricMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 小程序身体数据服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppBodyService {

    private final BodyMetricMapper bodyMetricMapper;
    private final UserMapper userMapper;

    /**
     * 获取身体数据统计
     */
    public BodyStatsResponse getStats() {
        Long userId = getCurrentUserId();

        // 获取最新记录
        LambdaQueryWrapper<BodyMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BodyMetric::getUserId, userId)
               .orderByDesc(BodyMetric::getRecordDate)
               .last("LIMIT 1");
        BodyMetric latest = bodyMetricMapper.selectOne(wrapper);

        // 获取用户目标体重
        User user = userMapper.selectById(userId);
        BigDecimal targetWeight = user != null ? user.getTargetWeightKg() : null;

        // 计算体重变化（最近7天）
        BigDecimal weightChange = BigDecimal.ZERO;
        if (latest != null) {
            LambdaQueryWrapper<BodyMetric> weekAgoWrapper = new LambdaQueryWrapper<>();
            weekAgoWrapper.eq(BodyMetric::getUserId, userId)
                         .le(BodyMetric::getRecordDate, LocalDate.now().minusDays(7))
                         .orderByDesc(BodyMetric::getRecordDate)
                         .last("LIMIT 1");
            BodyMetric weekAgo = bodyMetricMapper.selectOne(weekAgoWrapper);
            if (weekAgo != null && weekAgo.getWeightKg() != null && latest.getWeightKg() != null) {
                weightChange = latest.getWeightKg().subtract(weekAgo.getWeightKg());
            }
        }

        // 获取最近90天的趋势数据
        LocalDate startDate = LocalDate.now().minusDays(90);
        LambdaQueryWrapper<BodyMetric> trendWrapper = new LambdaQueryWrapper<>();
        trendWrapper.eq(BodyMetric::getUserId, userId)
                   .ge(BodyMetric::getRecordDate, startDate)
                   .orderByAsc(BodyMetric::getRecordDate);
        List<BodyMetric> trendRecords = bodyMetricMapper.selectList(trendWrapper);

        List<BodyStatsResponse.TrendItem> weightTrend = new ArrayList<>();
        List<BodyStatsResponse.TrendItem> fatTrend = new ArrayList<>();
        List<BodyStatsResponse.TrendItem> muscleTrend = new ArrayList<>();
        List<BodyStatsResponse.TrendItem> bmiTrend = new ArrayList<>();

        for (BodyMetric record : trendRecords) {
            String date = record.getRecordDate().toString();
            if (record.getWeightKg() != null) {
                weightTrend.add(new BodyStatsResponse.TrendItem(date, record.getWeightKg()));
            }
            if (record.getBodyFatPct() != null) {
                fatTrend.add(new BodyStatsResponse.TrendItem(date, record.getBodyFatPct()));
            }
            if (record.getMuscleMassKg() != null) {
                muscleTrend.add(new BodyStatsResponse.TrendItem(date, record.getMuscleMassKg()));
            }
            if (record.getBmi() != null) {
                bmiTrend.add(new BodyStatsResponse.TrendItem(date, record.getBmi()));
            }
        }

        BodyStatsResponse response = new BodyStatsResponse();
        response.setCurrentWeight(latest != null ? latest.getWeightKg() : null);
        response.setTargetWeight(targetWeight);
        response.setWeightChange(weightChange);
        response.setBodyFatPct(latest != null ? latest.getBodyFatPct() : null);
        response.setBmi(latest != null ? latest.getBmi() : null);
        response.setMilestones(new ArrayList<>());
        response.setWeightTrend(weightTrend);
        response.setFatTrend(fatTrend);
        response.setMuscleTrend(muscleTrend);
        response.setBmiTrend(bmiTrend);

        return response;
    }

    /**
     * 记录身体数据
     */
    public void record(RecordBodyRequest request) {
        Long userId = getCurrentUserId();

        BodyMetric metric = new BodyMetric();
        metric.setUserId(userId);
        metric.setRecordDate(request.getRecordDate() != null ?
                LocalDate.parse(request.getRecordDate()) : LocalDate.now());
        metric.setWeightKg(request.getWeightKg());
        metric.setBodyFatPct(request.getBodyFatPct());
        metric.setMuscleMassKg(request.getMuscleMassKg());
        metric.setBmi(request.getBmi());
        metric.setChestCm(request.getChestCm());
        metric.setWaistCm(request.getWaistCm());
        metric.setHipCm(request.getHipCm());
        metric.setLeftArmCm(request.getLeftArmCm());
        metric.setRightArmCm(request.getRightArmCm());
        metric.setLeftThighCm(request.getLeftThighCm());
        metric.setRightThighCm(request.getRightThighCm());
        metric.setNote(request.getNote());
        metric.setCreatedAt(LocalDateTime.now());
        metric.setUpdatedAt(LocalDateTime.now());

        // 自动计算BMI
        if (metric.getWeightKg() != null) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getHeightCm() != null) {
                BigDecimal heightM = user.getHeightCm().divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal bmi = metric.getWeightKg().divide(heightM.multiply(heightM), 1, BigDecimal.ROUND_HALF_UP);
                metric.setBmi(bmi);
            }
        }

        // 检查今天是否已有记录，有则更新，无则插入
        LambdaQueryWrapper<BodyMetric> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BodyMetric::getUserId, userId)
                   .eq(BodyMetric::getRecordDate, metric.getRecordDate());
        BodyMetric existRecord = bodyMetricMapper.selectOne(existWrapper);

        if (existRecord != null) {
            metric.setId(existRecord.getId());
            metric.setCreatedAt(existRecord.getCreatedAt());
            metric.setUpdatedAt(LocalDateTime.now());
            bodyMetricMapper.updateById(metric);
            log.info("用户{}更新身体数据", userId);
        } else {
            bodyMetricMapper.insert(metric);
            log.info("用户{}记录身体数据", userId);
        }
    }

    /**
     * 获取里程碑列表
     */
    public MilestoneResponse getMilestones() {
        Long userId = getCurrentUserId();
        List<Milestone> milestones = new ArrayList<>();

        // 获取用户信息
        User user = userMapper.selectById(userId);
        BigDecimal targetWeight = user != null ? user.getTargetWeightKg() : null;

        // 获取所有记录，按日期排序
        LambdaQueryWrapper<BodyMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BodyMetric::getUserId, userId)
               .orderByAsc(BodyMetric::getRecordDate);
        List<BodyMetric> records = bodyMetricMapper.selectList(wrapper);

        int totalDays = records.size();
        BigDecimal latestWeight = !records.isEmpty() ? records.get(records.size() - 1).getWeightKg() : null;

        // 首次记录里程碑
        if (!records.isEmpty()) {
            BodyMetric first = records.get(0);
            Milestone m = new Milestone();
            m.setType("first_record");
            m.setTitle("初次记录");
            m.setDescription("开始记录身体数据，迈出第一步！");
            m.setAchievedAt(first.getRecordDate().toString());
            milestones.add(m);
        }

        // 连续记录里程碑
        if (totalDays >= 7) {
            Milestone m = new Milestone();
            m.setType("streak_7");
            m.setTitle("坚持7天");
            m.setDescription("连续记录7天，养成好习惯！");
            m.setAchievedAt(records.get(6).getRecordDate().toString());
            milestones.add(m);
        }
        if (totalDays >= 30) {
            Milestone m = new Milestone();
            m.setType("streak_30");
            m.setTitle("坚持30天");
            m.setDescription("连续记录30天，你真的很棒！");
            m.setAchievedAt(records.get(29).getRecordDate().toString());
            milestones.add(m);
        }
        if (totalDays >= 100) {
            Milestone m = new Milestone();
            m.setType("streak_100");
            m.setTitle("百日坚持");
            m.setDescription("记录100天，这是了不起的成就！");
            m.setAchievedAt(records.get(99).getRecordDate().toString());
            milestones.add(m);
        }

        // 体重变化里程碑（从首次到最新）
        if (records.size() >= 2) {
            BigDecimal firstWeight = records.get(0).getWeightKg();
            if (firstWeight != null && latestWeight != null) {
                BigDecimal change = latestWeight.subtract(firstWeight).abs();

                if (change.compareTo(BigDecimal.valueOf(1)) >= 0) {
                    Milestone m = new Milestone();
                    m.setType("weight_change_1kg");
                    m.setTitle("变化1公斤");
                    m.setDescription("体重变化了1公斤，继续加油！");
                    m.setAchievedAt(records.get(records.size() - 1).getRecordDate().toString());
                    milestones.add(m);
                }
                if (change.compareTo(BigDecimal.valueOf(5)) >= 0) {
                    Milestone m = new Milestone();
                    m.setType("weight_change_5kg");
                    m.setTitle("变化5公斤");
                    m.setDescription("体重变化了5公斤，效果显著！");
                    m.setAchievedAt(records.get(records.size() - 1).getRecordDate().toString());
                    milestones.add(m);
                }
                if (change.compareTo(BigDecimal.valueOf(10)) >= 0) {
                    Milestone m = new Milestone();
                    m.setType("weight_change_10kg");
                    m.setTitle("变化10公斤");
                    m.setDescription("体重变化了10公斤，太厉害了！");
                    m.setAchievedAt(records.get(records.size() - 1).getRecordDate().toString());
                    milestones.add(m);
                }
            }
        }

        // 达到目标体重里程碑
        if (latestWeight != null && targetWeight != null) {
            BigDecimal diff = latestWeight.subtract(targetWeight).abs();
            if (diff.compareTo(BigDecimal.valueOf(1)) <= 0) {
                Milestone m = new Milestone();
                m.setType("target_reached");
                m.setTitle("达成目标");
                m.setDescription("恭喜你达到目标体重！");
                m.setAchievedAt(records.get(records.size() - 1).getRecordDate().toString());
                milestones.add(m);
            }
        }

        MilestoneResponse response = new MilestoneResponse();
        response.setMilestones(milestones);
        response.setTotalDays(totalDays);
        response.setLatestWeight(latestWeight);
        response.setTargetWeight(targetWeight);

        return response;
    }

    /**
     * 获取身体数据趋势
     */
    public Map<String, Object> getTrend(String metric, Integer days) {
        Long userId = getCurrentUserId();
        LocalDate startDate = LocalDate.now().minusDays(days);

        LambdaQueryWrapper<BodyMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BodyMetric::getUserId, userId)
               .ge(BodyMetric::getRecordDate, startDate)
               .orderByAsc(BodyMetric::getRecordDate);
        List<BodyMetric> records = bodyMetricMapper.selectList(wrapper);

        List<String> dates = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        for (BodyMetric record : records) {
            dates.add(record.getRecordDate().format(DateTimeFormatter.ofPattern("MM-dd")));
            switch (metric) {
                case "weight":
                    values.add(record.getWeightKg());
                    break;
                case "bodyFat":
                    values.add(record.getBodyFatPct());
                    break;
                case "muscleMass":
                    values.add(record.getMuscleMassKg());
                    break;
                case "bmi":
                    values.add(record.getBmi());
                    break;
                case "waist":
                    values.add(record.getWaistCm());
                    break;
                default:
                    values.add(record.getWeightKg());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("metric", metric);
        result.put("dates", dates);
        result.put("values", values);

        return result;
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
