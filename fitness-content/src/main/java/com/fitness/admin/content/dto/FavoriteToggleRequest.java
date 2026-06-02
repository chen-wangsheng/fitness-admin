package com.fitness.admin.content.dto;

import lombok.Data;

/**
 * 收藏/取消收藏请求
 */
@Data
public class FavoriteToggleRequest {
    private Long exerciseId;
}
