package com.fitness.admin.community.dto;

import lombok.Data;

/**
 * 点赞响应
 */
@Data
public class LikeResponse {
    private Boolean liked;
    private Integer likeCount;
}
