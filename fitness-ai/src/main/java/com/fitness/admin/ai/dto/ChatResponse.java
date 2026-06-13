package com.fitness.admin.ai.dto;

import lombok.Data;

/**
 * AI对话响应
 */
@Data
public class ChatResponse {
    /** 会话ID */
    private Long sessionId;
    /** AI回复消息ID */
    private Long messageId;
    /** AI回复内容 */
    private String content;
    /** token消耗量 */
    private Integer tokenCount;
    /**
     * 消息状态(异步模式): 1-processing 2-completed 3-failed
     * 同步模式下始终为 2-completed
     */
    private Integer status;
}
