package com.fitness.admin.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.ai.entity.AiSafetyEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI安全事件日志Mapper
 */
@Mapper
public interface AiSafetyEventMapper extends BaseMapper<AiSafetyEvent> {
}
