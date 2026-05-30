package com.fitness.admin.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserAchievementMapper {

    List<Map<String, Object>> selectByUserId(@Param("userId") Long userId);
}
