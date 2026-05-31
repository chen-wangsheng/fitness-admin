package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ai_chat_message")
public class AiChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String ragRefs;
    private String structuredData;
    private Integer tokenCount;
    private Integer feedback;
    private LocalDateTime createdAt;
}
