package com.fitness.admin.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.ai.entity.KnowledgeBase;
import com.fitness.admin.ai.service.AiAnalyticsService;
import com.fitness.admin.ai.service.AiKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/ai-knowledge")
@RequiredArgsConstructor
public class AiKnowledgeController extends BaseController {

    private final AiKnowledgeService aiKnowledgeService;
    private final AiAnalyticsService aiAnalyticsService;

    @Operation(summary = "知识库列表")
    @GetMapping("/list")
    public R<PageResult<KnowledgeBase>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<KnowledgeBase> page = aiKnowledgeService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "获取知识分类")
    @GetMapping("/categories")
    public R<List<String>> categories() {
        return success(Arrays.asList("training", "recovery", "nutrition", "injury", "sleep"));
    }

    @Operation(summary = "保存知识")
    @PostMapping
    public R<Void> save(@RequestBody KnowledgeBase knowledgeBase) {
        aiKnowledgeService.save(knowledgeBase);
        return success();
    }

    @Operation(summary = "删除知识")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        aiKnowledgeService.delete(id);
        return success();
    }

    @Operation(summary = "知识使用统计")
    @GetMapping("/analytics/usage")
    public R<List<Map<String, Object>>> usage() {
        return R.ok(aiAnalyticsService.getKnowledgeUsage());
    }

    @Operation(summary = "RAG命中率")
    @GetMapping("/analytics/rag-hit-rate")
    public R<List<Map<String, Object>>> ragHitRate() {
        return R.ok(aiAnalyticsService.getRagHitRate());
    }
}
