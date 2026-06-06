package com.fitness.admin.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.service.AiAnalyticsService;
import com.fitness.admin.ai.service.AiChatMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "AI对话监控")
@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class AiChatMonitorController extends BaseController {

    private final AiChatMonitorService aiChatMonitorService;
    private final AiAnalyticsService aiAnalyticsService;

    @Operation(summary = "会话列表")
    @GetMapping("/sessions")
    public R<PageResult<AiChatSession>> sessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiChatSession> page = aiChatMonitorService.querySessionPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "会话详情")
    @GetMapping("/sessions/{id}")
    public R<AiChatSession> sessionDetail(@PathVariable Long id) {
        return R.ok(aiChatMonitorService.getSessionDetail(id));
    }

    @Operation(summary = "消息列表")
    @GetMapping("/messages/{sessionId}")
    public R<PageResult<AiChatMessage>> messages(@PathVariable Long sessionId,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiChatMessage> page = aiChatMonitorService.queryMessagePage(sessionId, pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "反馈统计")
    @GetMapping("/feedback-stats")
    public R<Map<String, Object>> feedbackStats() {
        Map<String, Object> stats = aiChatMonitorService.getFeedbackStats();
        return R.ok(stats);
    }

    @Operation(summary = "标记问题")
    @PostMapping("/sessions/{sessionId}/messages/{msgId}/issue")
    public R<Void> markIssue(@PathVariable Long sessionId,
                             @PathVariable Long msgId,
                             @RequestBody Map<String, String> body) {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        aiChatMonitorService.markIssue(sessionId, msgId, adminUserId, body.get("reason"));
        return R.ok();
    }

    @Operation(summary = "更新问题纠正")
    @PutMapping("/sessions/{sessionId}/messages/{msgId}/issue")
    public R<Void> updateIssue(@PathVariable Long sessionId,
                               @PathVariable Long msgId,
                               @RequestBody Map<String, String> body) {
        aiChatMonitorService.updateIssue(sessionId, msgId, body.get("correction"));
        return R.ok();
    }

    @Operation(summary = "加入知识库")
    @PostMapping("/sessions/{sessionId}/messages/{msgId}/to-knowledge")
    public R<Void> toKnowledge(@PathVariable Long sessionId,
                               @PathVariable Long msgId) {
        aiChatMonitorService.markToKnowledge(sessionId, msgId);
        return R.ok();
    }

    @Operation(summary = "对话趋势")
    @GetMapping("/analytics/chat-trend")
    public R<List<Map<String, Object>>> chatTrend() {
        return R.ok(aiAnalyticsService.getChatTrend());
    }

    @Operation(summary = "满意度趋势")
    @GetMapping("/analytics/satisfaction-trend")
    public R<List<Map<String, Object>>> satisfactionTrend() {
        return R.ok(aiAnalyticsService.getSatisfactionTrend());
    }

    @Operation(summary = "热门问题")
    @GetMapping("/analytics/hot-questions")
    public R<List<Map<String, Object>>> hotQuestions() {
        return R.ok(aiAnalyticsService.getHotQuestions());
    }

    @Operation(summary = "响应时间")
    @GetMapping("/analytics/response-time")
    public R<List<Map<String, Object>>> responseTime() {
        return R.ok(aiAnalyticsService.getResponseTime());
    }
}
