package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI安全事件日志
 */
@Data
@TableName("ai_safety_event")
public class AiSafetyEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 触发的规则ID
     */
    private Integer ruleId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 触发消息内容
     */
    private String messageContent;

    /**
     * 采取的行动
     */
    private String actionTaken;

    /**
     * 发送的响应
     */
    private String responseSent;

    /**
     * 匹配延迟(ms)
     */
    private Integer matchLatencyMs;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
