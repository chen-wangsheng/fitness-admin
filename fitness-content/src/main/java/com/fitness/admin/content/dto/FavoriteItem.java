package com.fitness.admin.content.dto;

import lombok.Data;

/**
 * 收藏项
 */
@Data
public class FavoriteItem {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private String exerciseNameEn;
    private String categoryName;
    private String difficulty;
    private String exerciseType;
    private String equipment;
    private String demoImageUrl;
    private Integer isCompound;
}
