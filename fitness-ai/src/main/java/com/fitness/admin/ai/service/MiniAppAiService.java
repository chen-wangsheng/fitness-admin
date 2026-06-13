package com.fitness.admin.ai.service;

import com.fitness.admin.ai.dto.*;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 小程序 AI 服务门面(Facade),将原 915 行大类的功能拆分后统一暴露给 controller。
 *
 * <p>内部委托给:
 * <ul>
 *   <li>{@link MiniAppChatService} - 对话 / 消息 / 反馈 / 会话列表</li>
 *   <li>{@link MiniAppPlanGenerateService} - AI 计划生成 / 确认 / 详情 / 列表</li>
 * </ul>
 *
 * <p>保留 {@code MiniAppAiService} 名称以兼容现有 controller 与单元测试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppAiService {

    private final MiniAppChatService chatService;
    private final MiniAppPlanGenerateService planGenerateService;

    public ChatResponse sendChatMessage(ChatRequest request) {
        return chatService.sendChatMessage(request);
    }

    public ChatResponse pollMessage(Long messageId) {
        return chatService.pollMessage(messageId);
    }

    public PageResult<AiChatMessage> getChatMessages(Long sessionId, Integer pageNum, Integer pageSize) {
        return chatService.getChatMessages(sessionId, pageNum, pageSize);
    }

    public PageResult<AiChatSession> getChatSessions(Integer pageNum, Integer pageSize) {
        return chatService.getChatSessions(pageNum, pageSize);
    }

    public void feedback(Long sessionId, Long msgId, Integer feedback) {
        chatService.feedback(sessionId, msgId, feedback);
    }

    public GeneratePlanResponse generatePlan(GeneratePlanRequest request) {
        return planGenerateService.generatePlan(request);
    }

    public ConfirmPlanResponse confirmPlan(Long id) {
        return planGenerateService.confirmPlan(id);
    }

    public AiPlan getPlanDetail(Long id) {
        return planGenerateService.getPlanDetail(id);
    }

    public PageResult<AiPlan> getPlanList(Integer pageNum, Integer pageSize, String status) {
        return planGenerateService.getPlanList(pageNum, pageSize, status);
    }
}
