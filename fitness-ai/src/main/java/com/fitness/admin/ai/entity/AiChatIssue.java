package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_issue")
public class AiChatIssue extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long messageId;

    private Long sessionId;

    private Long adminUserId;

    private String issueType;

    private String description;

    private String correctAnswer;

    private Boolean knowledgeAdded;
}
