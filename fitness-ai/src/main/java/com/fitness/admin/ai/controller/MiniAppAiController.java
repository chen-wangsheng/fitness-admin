package com.fitness.admin.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fitness.admin.ai.dto.*;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.service.MiniAppAiService;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序AI接口
 */
@Tag(name = "小程序-AI模块")
@RestController
@RequestMapping("/miniapp/ai")
@RequiredArgsConstructor
@SaCheckLogin
public class MiniAppAiController extends BaseController {

    private final MiniAppAiService miniAppAiService;

    @Operation(summary = "发送AI对话消息")
    @PostMapping("/chat/send")
    public R<ChatResponse> sendChatMessage(@Valid @RequestBody ChatRequest request) {
        return success(miniAppAiService.sendChatMessage(request));
    }

    @Operation(summary = "会话消息列表")
    @GetMapping("/chat/{sessionId}/messages")
    public R<PageResult<AiChatMessage>> getChatMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return success(miniAppAiService.getChatMessages(sessionId, pageNum, pageSize));
    }

    @Operation(summary = "会话列表")
    @GetMapping("/chat/sessions")
    public R<PageResult<AiChatSession>> getChatSessions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return success(miniAppAiService.getChatSessions(pageNum, pageSize));
    }

    @Operation(summary = "消息反馈")
    @PostMapping("/chat/{sessionId}/messages/{msgId}/feedback")
    public R<Void> feedback(@PathVariable Long sessionId,
                            @PathVariable Long msgId,
                            @RequestBody FeedbackRequest request) {
        miniAppAiService.feedback(sessionId, msgId, request.getFeedback());
        return success();
    }

    @Operation(summary = "生成AI计划")
    @PostMapping("/plan/generate")
    public R<GeneratePlanResponse> generatePlan(@RequestBody GeneratePlanRequest request) {
        return success(miniAppAiService.generatePlan(request));
    }

    @Operation(summary = "确认AI计划")
    @PostMapping("/plan/{id}/confirm")
    public R<ConfirmPlanResponse> confirmPlan(@PathVariable Long id) {
        return success(miniAppAiService.confirmPlan(id));
    }

    @Operation(summary = "AI计划详情")
    @GetMapping("/plan/{id}")
    public R<AiPlan> getPlanDetail(@PathVariable Long id) {
        return success(miniAppAiService.getPlanDetail(id));
    }

    @Operation(summary = "AI计划列表")
    @GetMapping("/plan/list")
    public R<PageResult<AiPlan>> getPlanList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return success(miniAppAiService.getPlanList(pageNum, pageSize, status));
    }
}
