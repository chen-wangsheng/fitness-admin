package com.fitness.admin.community.dto;

import lombok.Data;

import java.util.List;

/**
 * 发布帖子请求
 */
@Data
public class CreatePostRequest {
    private String content;
    private List<String> images;
    private Long workoutLogId;
}
