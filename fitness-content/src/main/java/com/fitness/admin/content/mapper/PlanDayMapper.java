package com.fitness.admin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.content.entity.PlanDay;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlanDayMapper extends BaseMapper<PlanDay> {

    List<PlanDay> selectByPlanId(Long planId);
}
