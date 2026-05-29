package com.fitness.admin.workout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.workout.entity.WorkoutLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkoutLogMapper extends BaseMapper<WorkoutLog> {
}
