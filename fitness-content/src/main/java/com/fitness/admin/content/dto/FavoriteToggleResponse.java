package com.fitness.admin.content.dto;

import lombok.Data;

/**
 * 收藏/取消收藏响应
 */
@Data
public class FavoriteToggleResponse {
    private Boolean isFavorited;
}
