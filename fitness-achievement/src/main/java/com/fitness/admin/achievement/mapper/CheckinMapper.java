package com.fitness.admin.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.achievement.entity.Checkin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface CheckinMapper extends BaseMapper<Checkin> {

    /**
     * 查询指定日期范围内的每日打卡人数
     */
    List<Map<String, Object>> selectDailyCheckinStats(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 查询连续打卡排行榜（前N名）
     */
    List<Map<String, Object>> selectLeaderboard(@Param("limit") Integer limit);

    /**
     * 查询今日打卡人数
     */
    Integer selectTodayCheckinCount(@Param("today") LocalDate today);
}
