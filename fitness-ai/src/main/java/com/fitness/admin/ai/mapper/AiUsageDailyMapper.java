package com.fitness.admin.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.ai.entity.AiUsageDaily;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI每日使用统计Mapper
 */
@Mapper
public interface AiUsageDailyMapper extends BaseMapper<AiUsageDaily> {
}
