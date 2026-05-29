package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_session")
public class AiChatSession extends BaseEntity {

    private Long userId;
    private String sessionType;
    private String title;
    private Integer messageCount;
    private Integer status;
}
