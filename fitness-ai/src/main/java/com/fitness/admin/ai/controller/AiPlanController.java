package com.fitness.admin.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.ai.entity.AiAdjustmentConfig;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.entity.PlanLoadAdjustment;
import com.fitness.admin.ai.service.AiAnalyticsService;
import com.fitness.admin.ai.service.AiPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "AI计划管理")
@RestController
@RequestMapping("/ai-plan")
@RequiredArgsConstructor
public class AiPlanController extends BaseController {

    private final AiPlanService aiPlanService;
    private final AiAnalyticsService aiAnalyticsService;

    @Operation(summary = "AI计划列表")
    @GetMapping("/list")
    public R<PageResult<AiPlan>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiPlan> page = aiPlanService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "AI计划详情")
    @GetMapping("/{id}")
    public R<AiPlan> detail(@PathVariable Long id) {
        return R.ok(aiPlanService.getDetail(id));
    }

    @Operation(summary = "计划调整记录")
    @GetMapping("/{id}/adjustments")
    public R<List<PlanLoadAdjustment>> adjustments(@PathVariable Long id) {
        return R.ok(aiPlanService.getAdjustments(id));
    }

    @Operation(summary = "微调规则配置")
    @GetMapping("/adjustment-rules")
    public R<List<AiAdjustmentConfig>> adjustmentRules() {
        return R.ok(aiPlanService.getAdjustmentRules());
    }

    @Operation(summary = "更新微调规则")
    @PutMapping("/adjustment-rules")
    public R<Void> updateAdjustmentRules(@RequestBody List<AiAdjustmentConfig> rules) {
        aiPlanService.updateAdjustmentRules(rules);
        return success();
    }

    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        aiPlanService.updateStatus(id, status);
        return success();
    }

    @Operation(summary = "转换为系统计划")
    @PostMapping("/{id}/convert-to-system")
    public R<Long> convertToSystem(@PathVariable Long id) {
        Long planId = aiPlanService.convertToSystem(id);
        return R.ok(planId);
    }

    @Operation(summary = "总览数据")
    @GetMapping("/analytics/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(aiAnalyticsService.getOverview());
    }

    @Operation(summary = "计划统计")
    @GetMapping("/analytics/plan-stats")
    public R<Map<String, Object>> planStats() {
        return R.ok(aiAnalyticsService.getPlanStats());
    }

    @Operation(summary = "计划统计(前端)")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(aiAnalyticsService.getPlanStats());
    }

    @Operation(summary = "Token用量")
    @GetMapping("/analytics/token-usage")
    public R<List<Map<String, Object>>> tokenUsage() {
        return R.ok(aiAnalyticsService.getTokenUsage());
    }
}
