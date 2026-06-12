package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.admin.ai.entity.AiChatIssue;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.mapper.AiChatIssueMapper;
import com.fitness.admin.ai.mapper.AiChatMessageMapper;
import com.fitness.admin.ai.mapper.AiChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatMonitorService {

    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatIssueMapper aiChatIssueMapper;

    public Page<AiChatSession> querySessionPage(Integer pageNum, Integer pageSize, Long userId) {
        Page<AiChatSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(AiChatSession::getUserId, userId);
        }
        wrapper.orderByDesc(AiChatSession::getUpdatedAt);
        return aiChatSessionMapper.selectPage(page, wrapper);
    }

    public Page<AiChatMessage> queryMessagePage(Long sessionId, Integer pageNum, Integer pageSize) {
        Page<AiChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
               .orderByAsc(AiChatMessage::getId);
        Page<AiChatMessage> result = aiChatMessageMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::parseRagRefs);
        return result;
    }

    private void parseRagRefs(AiChatMessage message) {
        if (message.getRagRefs() == null || message.getRagRefs().isBlank()) {
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<AiChatMessage.RagReference> refs = mapper.readValue(
                    message.getRagRefs(), new TypeReference<>() {});
            message.setRagReferences(refs);
        } catch (Exception e) {
            log.warn("解析ragRefs失败: {}", message.getRagRefs(), e);
        }
    }

    public Map<String, Object> getFeedbackStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalSessions = aiChatSessionMapper.selectCount(null);
        long totalMessages = aiChatMessageMapper.selectCount(null);
        long thumbsUp = aiChatMessageMapper.selectCount(
                new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getFeedback, 1));
        long thumbsDown = aiChatMessageMapper.selectCount(
                new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getFeedback, -1));
        stats.put("totalSessions", totalSessions);
        stats.put("totalMessages", totalMessages);
        stats.put("thumbsUp", thumbsUp);
        stats.put("thumbsDown", thumbsDown);
        return stats;
    }

    public AiChatSession getSessionDetail(Long sessionId) {
        return aiChatSessionMapper.selectById(sessionId);
    }

    public void markIssue(Long sessionId, Long messageId, Long adminUserId, String reason) {
        AiChatIssue issue = new AiChatIssue();
        issue.setSessionId(sessionId);
        issue.setMessageId(messageId);
        issue.setAdminUserId(adminUserId);
        issue.setIssueType("inaccurate");
        issue.setDescription(reason);
        issue.setKnowledgeAdded(false);
        aiChatIssueMapper.insert(issue);
    }

    public void updateIssue(Long sessionId, Long messageId, String correction) {
        LambdaUpdateWrapper<AiChatIssue> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiChatIssue::getSessionId, sessionId)
                .eq(AiChatIssue::getMessageId, messageId)
                .set(AiChatIssue::getCorrectAnswer, correction);
        aiChatIssueMapper.update(null, wrapper);
    }

    public void markToKnowledge(Long sessionId, Long messageId) {
        LambdaUpdateWrapper<AiChatIssue> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiChatIssue::getSessionId, sessionId)
                .eq(AiChatIssue::getMessageId, messageId)
                .set(AiChatIssue::getKnowledgeAdded, true);
        aiChatIssueMapper.update(null, wrapper);
    }
}
