package com.fitness.admin.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.ai.entity.AiPromptTemplate;
import com.fitness.admin.ai.entity.AiSafetyRule;
import com.fitness.admin.ai.service.AiPromptTemplateService;
import com.fitness.admin.ai.service.AiSafetyRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI安全与Prompt")
@RestController
@RequestMapping("/ai-safety")
@RequiredArgsConstructor
public class AiSafetyController extends BaseController {

    private final AiSafetyRuleService aiSafetyRuleService;
    private final AiPromptTemplateService aiPromptTemplateService;

    @Operation(summary = "安全规则列表")
    @GetMapping("/rules")
    public R<PageResult<AiSafetyRule>> rules(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiSafetyRule> page = aiSafetyRuleService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "保存安全规则")
    @PostMapping("/rule")
    public R<Void> saveRule(@RequestBody AiSafetyRule rule) {
        aiSafetyRuleService.save(rule);
        return success();
    }

    @Operation(summary = "删除安全规则")
    @DeleteMapping("/rule/{id}")
    public R<Void> deleteRule(@PathVariable Long id) {
        aiSafetyRuleService.delete(id);
        return success();
    }

    @Operation(summary = "Prompt模板列表")
    @GetMapping("/prompts")
    public R<PageResult<AiPromptTemplate>> prompts(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiPromptTemplate> page = aiPromptTemplateService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "保存Prompt模板")
    @PostMapping("/prompt")
    public R<Void> savePrompt(@RequestBody AiPromptTemplate template) {
        aiPromptTemplateService.save(template);
        return success();
    }

    @Operation(summary = "删除Prompt模板")
    @DeleteMapping("/prompt/{id}")
    public R<Void> deletePrompt(@PathVariable Long id) {
        aiPromptTemplateService.delete(id);
        return success();
    }
}
