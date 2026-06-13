package com.fitness.admin.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("ai_chat_message")
public class AiChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String ragRefs;
    private String structuredData;
    private Integer tokenCount;
    private Integer feedback;
    private LocalDateTime createdAt;

    /** 流式分片序号(从 0 开始),NULL 表示非流式或合并后的消息 */
    private Integer chunkIndex;

    /** 总分片数(仅首片记录) */
    private Integer chunkTotal;

    /** 是否流式消息: 1-是 0-否 */
    private Integer isStream;

    /** 流式状态: 1-生成中 2-已完成 3-失败 */
    private Integer streamStatus;

    /** 解析后的RAG引用列表，由Service层填充 */
    @TableField(exist = false)
    private List<RagReference> ragReferences;

    @Data
    public static class RagReference {
        private Long id;
        private String source;
        private Double score;
        private String title;
        private String categoryName;
    }
}
