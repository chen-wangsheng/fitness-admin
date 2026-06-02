package com.fitness.admin.ai.dto;

import lombok.Data;

/**
 * AI对话请求
 */
@Data
public class ChatRequest {
    /** 会话ID，为空则创建新会话 */
    private Long sessionId;
    /** 用户消息 */
    private String message;
}
