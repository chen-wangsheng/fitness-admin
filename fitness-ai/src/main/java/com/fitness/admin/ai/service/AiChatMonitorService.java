package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiChatIssue;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.mapper.AiChatIssueMapper;
import com.fitness.admin.ai.mapper.AiChatMessageMapper;
import com.fitness.admin.ai.mapper.AiChatSessionMapper;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatMonitorService {

    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiChatIssueMapper aiChatIssueMapper;
    private final UserMapper userMapper;

    public Page<AiChatSession> querySessionPage(Integer pageNum, Integer pageSize) {
        Page<AiChatSession> page = new Page<>(pageNum, pageSize);
        return aiChatSessionMapper.selectPage(page, null);
    }

    public Page<AiChatMessage> queryMessagePage(Long sessionId, Integer pageNum, Integer pageSize) {
        Page<AiChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId);
        return aiChatMessageMapper.selectPage(page, wrapper);
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

    public Map<String, Object> getSessionDetail(Long sessionId) {
        AiChatSession session = aiChatSessionMapper.selectById(sessionId);
        if (session == null) {
            return null;
        }
        User user = userMapper.selectById(session.getUserId());
        long thumbsUp = aiChatMessageMapper.selectCount(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .eq(AiChatMessage::getFeedback, 1));
        long thumbsDown = aiChatMessageMapper.selectCount(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .eq(AiChatMessage::getFeedback, -1));

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("userName", user != null ? user.getNickname() : "未知用户");
        result.put("messageCount", session.getMessageCount());
        result.put("thumbsUp", thumbsUp);
        result.put("thumbsDown", thumbsDown);
        result.put("status", session.getStatus() == 1 ? "active" : "archived");
        result.put("startTime", session.getCreatedAt());
        result.put("lastMessageTime", session.getUpdatedAt());
        return result;
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
