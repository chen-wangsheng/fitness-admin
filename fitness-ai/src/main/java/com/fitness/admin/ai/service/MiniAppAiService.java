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
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    // TODO: 注入AI调用服务
    // private final AiChatService aiChatService;
    // private final AiPlanGeneratorService aiPlanGeneratorService;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 发送AI对话消息(SSE)
     */
    public SseEmitter sendChatMessage(ChatRequest request) {
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

        // 创建SSE发射器
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时

        // 异步处理AI响应
        executorService.execute(() -> {
            try {
                // TODO: 调用AI服务获取响应
                // 这里模拟AI响应
                String aiResponse = "这是一个模拟的AI响应。实际应调用LLM服务。";

                // 保存AI消息
                AiChatMessage aiMessage = new AiChatMessage();
                aiMessage.setSessionId(session.getId());
                aiMessage.setRole("assistant");
                aiMessage.setContent(aiResponse);
                aiMessage.setTokenCount(aiResponse.length()); // 简单估算
                aiMessage.setCreatedAt(LocalDateTime.now());
                messageMapper.insert(aiMessage);

                // 更新会话消息数
                session.setMessageCount(session.getMessageCount() + 1);
                session.setUpdatedAt(LocalDateTime.now());
                sessionMapper.updateById(session);

                // 发送SSE事件
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(Map.of(
                                "type", "content",
                                "content", aiResponse
                        )));

                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(Map.of(
                                "type", "end",
                                "messageId", aiMessage.getId(),
                                "tokenCount", aiMessage.getTokenCount()
                        )));

                emitter.complete();
            } catch (Exception e) {
                log.error("AI对话处理失败", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
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
            throw new BizException("请先登录");
        }
        return userId;
    }
}
