package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.dto.*;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.mapper.AiChatMessageMapper;
import com.fitness.admin.ai.mapper.AiChatSessionMapper;
import com.fitness.admin.ai.mapper.AiPlanMapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 小程序AI服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppAiService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final AiPlanMapper planMapper;

    private final AiService aiService;

    /**
     * 发送AI对话消息
     */
    public ChatResponse sendChatMessage(ChatRequest request) {
        Long userId = getCurrentUserId();

        // 创建或获取会话
        AiChatSession session;
        if (request.getSessionId() != null) {
            session = sessionMapper.selectById(request.getSessionId());
            if (session == null || !session.getUserId().equals(userId)) {
                throw new BizException("会话不存在");
            }
        } else {
            session = createNewSession(userId);
        }

        // 保存用户消息
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        userMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 更新会话消息数
        session.setMessageCount(session.getMessageCount() + 1);
        if (session.getTitle() == null) {
            session.setTitle(request.getMessage().substring(0, Math.min(request.getMessage().length(), 50)));
        }
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 获取历史消息用于上下文
        List<AiService.ChatMessage> chatMessages = buildChatMessages(session.getId());

        // 调用AI服务获取响应
        String aiResponse;
        try {
            aiResponse = aiService.chat(chatMessages);
        } catch (Exception e) {
            log.error("AI服务调用失败，使用备用响应", e);
            aiResponse = "抱歉，AI服务暂时不可用，请稍后再试。错误信息：" + e.getMessage();
        }

        // 保存AI消息
        AiChatMessage aiMessage = new AiChatMessage();
        aiMessage.setSessionId(session.getId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        aiMessage.setTokenCount(aiResponse.length());
        aiMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(aiMessage);

        // 更新会话消息数
        session.setMessageCount(session.getMessageCount() + 1);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        ChatResponse response = new ChatResponse();
        response.setSessionId(session.getId());
        response.setMessageId(aiMessage.getId());
        response.setContent(aiResponse);
        response.setTokenCount(aiMessage.getTokenCount());
        return response;
    }

    /**
     * 获取会话消息列表
     */
    public PageResult<AiChatMessage> getChatMessages(Long sessionId, Integer pageNum, Integer pageSize) {
        Long userId = getCurrentUserId();

        // 验证会话归属
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BizException("会话不存在");
        }

        Page<AiChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
               .orderByAsc(AiChatMessage::getCreatedAt);
        Page<AiChatMessage> result = messageMapper.selectPage(page, wrapper);

        return PageResult.of(result);
    }

    /**
     * 获取会话列表
     */
    public PageResult<AiChatSession> getChatSessions(Integer pageNum, Integer pageSize) {
        Long userId = getCurrentUserId();

        Page<AiChatSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getUserId, userId)
               .orderByDesc(AiChatSession::getUpdatedAt);
        Page<AiChatSession> result = sessionMapper.selectPage(page, wrapper);

        return PageResult.of(result);
    }

    /**
     * 构建聊天消息列表（用于发送给AI服务）
     */
    private List<AiService.ChatMessage> buildChatMessages(Long sessionId) {
        List<AiService.ChatMessage> messages = new ArrayList<>();

        // 添加系统提示词
        messages.add(new AiService.ChatMessage("system",
                "你是一个专业的AI健身助手，名叫FitBot。你擅长制定训练计划、解答健身问题、提供营养建议。请用友好专业的语气回答。"));

        // 获取最近20条消息作为上下文
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, sessionId)
               .orderByDesc(AiChatMessage::getCreatedAt)
               .last("LIMIT 20");
        List<AiChatMessage> history = messageMapper.selectList(wrapper);

        // 反转顺序（从旧到新）
        for (int i = history.size() - 1; i >= 0; i--) {
            AiChatMessage msg = history.get(i);
            messages.add(new AiService.ChatMessage(msg.getRole(), msg.getContent()));
        }

        return messages;
    }

    /**
     * 消息反馈
     */
    public void feedback(Long sessionId, Long msgId, Integer feedback) {
        Long userId = getCurrentUserId();

        // 验证会话归属
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BizException("会话不存在");
        }

        AiChatMessage message = messageMapper.selectById(msgId);
        if (message == null || !message.getSessionId().equals(sessionId)) {
            throw new BizException("消息不存在");
        }

        message.setFeedback(feedback);
        messageMapper.updateById(message);
    }

    /**
     * 生成AI计划
     */
    @Transactional
    public GeneratePlanResponse generatePlan(GeneratePlanRequest request) {
        Long userId = getCurrentUserId();

        // TODO: 调用AI计划生成服务
        // 这里返回模拟数据
        AiPlan aiPlan = new AiPlan();
        aiPlan.setUserId(userId);
        aiPlan.setPrompt(request.toString());
        aiPlan.setGoal(request.getGoal());
        aiPlan.setDaysPerWeek(request.getDaysPerWeek());
        aiPlan.setSplitType("full_body");
        aiPlan.setExplanation("根据您的目标和可用时间，推荐全身训练方案");
        aiPlan.setVersion(1);
        aiPlan.setConverted(0);
        aiPlan.setStatus("draft");
        aiPlan.setCreatedAt(LocalDateTime.now());
        aiPlan.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(aiPlan);

        GeneratePlanResponse response = new GeneratePlanResponse();
        response.setAiPlanId(aiPlan.getId());
        response.setSplitType(aiPlan.getSplitType());
        response.setExplanation(aiPlan.getExplanation());
        response.setWeeklyPlan(new ArrayList<>());
        response.setDisclaimer("本计划由AI生成，仅供参考。请根据自身情况调整。");

        return response;
    }

    /**
     * 确认AI计划
     */
    @Transactional
    public ConfirmPlanResponse confirmPlan(Long id) {
        Long userId = getCurrentUserId();

        AiPlan aiPlan = planMapper.selectById(id);
        if (aiPlan == null || !aiPlan.getUserId().equals(userId)) {
            throw new BizException("计划不存在");
        }

        // TODO: 将AI计划转换为正式训练计划
        // 1. 创建workout_plan记录
        // 2. 创建plan_day记录
        // 3. 创建plan_day_exercise记录
        Long workoutPlanId = null; // TODO: 返回创建的计划ID

        aiPlan.setStatus("confirmed");
        aiPlan.setConverted(1);
        aiPlan.setConvertedPlanId(workoutPlanId);
        aiPlan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(aiPlan);

        ConfirmPlanResponse response = new ConfirmPlanResponse();
        response.setAiPlanId(aiPlan.getId());
        response.setWorkoutPlanId(workoutPlanId);
        response.setStatus("confirmed");

        return response;
    }

    /**
     * 获取AI计划详情
     */
    public AiPlan getPlanDetail(Long id) {
        Long userId = getCurrentUserId();

        AiPlan aiPlan = planMapper.selectById(id);
        if (aiPlan == null || !aiPlan.getUserId().equals(userId)) {
            throw new BizException("计划不存在");
        }

        return aiPlan;
    }

    /**
     * 获取AI计划列表
     */
    public PageResult<AiPlan> getPlanList(Integer pageNum, Integer pageSize, String status) {
        Long userId = getCurrentUserId();

        Page<AiPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiPlan::getUserId, userId);
        if (status != null) {
            wrapper.eq(AiPlan::getStatus, status);
        }
        wrapper.orderByDesc(AiPlan::getCreatedAt);
        Page<AiPlan> result = planMapper.selectPage(page, wrapper);

        return PageResult.of(result);
    }

    /**
     * 创建新会话
     */
    private AiChatSession createNewSession(Long userId) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setSessionType("fitness");
        session.setMessageCount(0);
        session.setStatus(1);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userId;
    }
}
