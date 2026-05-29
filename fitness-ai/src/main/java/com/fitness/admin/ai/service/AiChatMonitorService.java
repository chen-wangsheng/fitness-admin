package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.mapper.AiChatMessageMapper;
import com.fitness.admin.ai.mapper.AiChatSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatMonitorService {

    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;

    public Page<AiChatSession> querySessionPage(Integer pageNum, Integer pageSize) {
        Page<AiChatSession> page = new Page<>(pageNum, pageSize);
        return aiChatSessionMapper.selectPage(page, null);
    }

    public Page<AiChatMessage> queryMessagePage(Long sessionId, Integer pageNum, Integer pageSize) {
        Page<AiChatMessage> page = new Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatMessage> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId);
        return aiChatMessageMapper.selectPage(page, wrapper);
    }
}
