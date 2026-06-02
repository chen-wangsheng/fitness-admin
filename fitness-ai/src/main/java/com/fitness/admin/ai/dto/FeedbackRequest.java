package com.fitness.admin.ai.dto;

import lombok.Data;

/**
 * 消息反馈请求
 */
@Data
public class FeedbackRequest {
    /** 反馈: 1=有用, -1=无用 */
    private Integer feedback;
}
