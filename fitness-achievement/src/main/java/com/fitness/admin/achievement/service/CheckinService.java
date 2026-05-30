package com.fitness.admin.achievement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.achievement.entity.Checkin;
import com.fitness.admin.achievement.mapper.CheckinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinMapper checkinMapper;

    public Page<Checkin> queryPage(Integer pageNum, Integer pageSize) {
        Page<Checkin> page = new Page<>(pageNum, pageSize);
        return checkinMapper.selectPage(page, null);
    }

    /**
     * 打卡统计（近N天每日打卡人数 + 今日打卡人数）
     */
    public Map<String, Object> getStats(Integer days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Map<String, Object>> dailyStats = checkinMapper.selectDailyCheckinStats(startDate, today);
        Integer todayCount = checkinMapper.selectTodayCheckinCount(today);

        Map<String, Object> result = new HashMap<>();
        result.put("todayCount", todayCount != null ? todayCount : 0);
        result.put("dailyStats", dailyStats);
        result.put("days", days);
        return result;
    }

    /**
     * 连续打卡排行榜
     */
    public List<Map<String, Object>> getLeaderboard(Integer limit) {
        return checkinMapper.selectLeaderboard(limit);
    }
}
