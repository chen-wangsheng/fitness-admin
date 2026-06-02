package com.fitness.admin.community.dto;

import lombok.Data;

import java.util.List;

/**
 * 帖子详情响应
 */
@Data
public class PostDetailResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String content;
    private List<String> images;
    private Long workoutLogId;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private String createdAt;
}
