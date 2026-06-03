package com.fitness.admin.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserAchievementQueryMapper {

    List<Map<String, Object>> selectByUserId(@Param("userId") Long userId);
}
