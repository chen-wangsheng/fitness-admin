package com.fitness.admin.ai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 生成AI计划请求
 */
@Data
public class GeneratePlanRequest {
    /** 训练目标 */
    private String goal;
    /** 可用器械 */
    private List<String> availableEquipment;
    /** 每周训练天数 */
    private Integer daysPerWeek;
    /** 体测数据 */
    private Map<String, Object> bodyMetrics;
    /** 额外说明 */
    private String additionalNotes;
}
