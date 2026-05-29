package com.fitness.admin.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.ai.entity.AiChatMessage;
import com.fitness.admin.ai.entity.AiChatSession;
import com.fitness.admin.ai.service.AiChatMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI对话监控")
@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class AiChatMonitorController extends BaseController {

    private final AiChatMonitorService aiChatMonitorService;

    @Operation(summary = "会话列表")
    @GetMapping("/sessions")
    public R<PageResult<AiChatSession>> sessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiChatSession> page = aiChatMonitorService.querySessionPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "消息列表")
    @GetMapping("/messages/{sessionId}")
    public R<PageResult<AiChatMessage>> messages(@PathVariable Long sessionId,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiChatMessage> page = aiChatMonitorService.queryMessagePage(sessionId, pageNum, pageSize);
        return page((Page) page);
    }
}
