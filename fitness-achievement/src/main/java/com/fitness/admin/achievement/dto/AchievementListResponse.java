package com.fitness.admin.achievement.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 成就列表响应
 */
@Data
public class AchievementListResponse {

    /**
     * 成就分类列表
     */
    private List<Map<String, Object>> categories;

    /**
     * 统计信息
     */
    private Map<String, Object> stats;
}
