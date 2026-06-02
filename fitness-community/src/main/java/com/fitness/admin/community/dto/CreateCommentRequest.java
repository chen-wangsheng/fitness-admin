package com.fitness.admin.community.dto;

import lombok.Data;

/**
 * 发表评论请求
 */
@Data
public class CreateCommentRequest {
    private String content;
    private Long parentId;
}
