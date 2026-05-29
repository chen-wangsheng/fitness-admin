package com.fitness.admin.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.ai.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}
