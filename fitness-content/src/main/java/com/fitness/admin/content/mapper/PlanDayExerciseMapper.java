package com.fitness.admin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.content.entity.PlanDayExercise;
import com.fitness.admin.content.vo.PlanExerciseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlanDayExerciseMapper extends BaseMapper<PlanDayExercise> {

    List<PlanExerciseVO> selectByPlanDayId(Long planDayId);
}
