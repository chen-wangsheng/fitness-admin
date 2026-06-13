package com.fitness.admin.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Tag(name = "AI安全与Prompt")
@RestController
@RequestMapping("/ai-safety")
@RequiredArgsConstructor
@SaCheckPermission("ai:safety:read")
public class AiSafetyController extends BaseController {

    private final AiSafetyRuleService aiSafetyRuleService;
    private final AiPromptTemplateService aiPromptTemplateService;

    @Operation(summary = "安全规则列表")
    @GetMapping("/rules")
    public R<PageResult<AiSafetyRule>> rules(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiSafetyRule> page = aiSafetyRuleService.queryPage(pageNum, pageSize);
        return page(page);
    }

    @Operation(summary = "规则详情")
    @GetMapping("/rule/{id}")
    public R<AiSafetyRule> ruleDetail(@PathVariable Long id) {
        return R.ok(aiSafetyRuleService.getById(id));
    }

    @Operation(summary = "保存安全规则")
    @PostMapping("/rule")
    @SaCheckPermission("ai:safety:create")
    public R<Void> saveRule(@RequestBody AiSafetyRule rule) {
        aiSafetyRuleService.save(rule);
        return success();
    }

    @Operation(summary = "更新安全规则")
    @PutMapping("/rule/{id}")
    @SaCheckPermission("ai:safety:update")
    public R<Void> updateRule(@PathVariable Long id, @RequestBody AiSafetyRule rule) {
        rule.setId(id);
        aiSafetyRuleService.save(rule);
        return success();
    }

    @Operation(summary = "删除安全规则")
    @DeleteMapping("/rule/{id}")
    @SaCheckPermission("ai:safety:delete")
    public R<Void> deleteRule(@PathVariable Long id) {
        aiSafetyRuleService.delete(id);
        return success();
    }

    @Operation(summary = "测试规则")
    @PostMapping("/rule/test")
    public R<Map<String, Object>> testRule(@RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        List<AiSafetyRule> rules = aiSafetyRuleService.list(
                new LambdaQueryWrapper<AiSafetyRule>().eq(AiSafetyRule::getIsEnabled, 1));

        List<Map<String, Object>> matched = new ArrayList<>();
        for (AiSafetyRule rule : rules) {
            boolean hit = false;
            if ("keyword".equals(rule.getMatchMode())) {
                String[] keywords = rule.getPattern().split("[,，]");
                for (String kw : keywords) {
                    if (text.contains(kw.trim())) {
                        hit = true;
                        break;
                    }
                }
            } else if ("regex".equals(rule.getMatchMode())) {
                try {
                    hit = Pattern.compile(rule.getPattern()).matcher(text).find();
                } catch (Exception e) {
                    // 规则 regex 非法,记日志后跳过该规则(非业务关键)
                    log.warn("AI safety rule #{} pattern invalid: {}", rule.getId(), e.getMessage());
                }
            }
            if (hit) {
                Map<String, Object> item = new HashMap<>();
                item.put("ruleId", rule.getId());
                item.put("ruleType", rule.getRuleType());
                item.put("action", rule.getAction());
                item.put("description", rule.getDescription());
                item.put("responseTemplate", rule.getResponseTemplate());
                matched.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (matched.isEmpty()) {
            result.put("matched", false);
        } else {
            Map<String, Object> first = matched.get(0);
            result.put("matched", true);
            result.put("ruleName", first.get("description"));
            result.put("action", first.get("action"));
        }
        return R.ok(result);
    }

    @Operation(summary = "安全事件列表")
    @GetMapping("/events")
    public R<List<Map<String, Object>>> events(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(Collections.emptyList());
    }

    @Operation(summary = "Prompt模板列表")
    @GetMapping("/prompts")
    public R<PageResult<AiPromptTemplate>> prompts(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiPromptTemplate> page = aiPromptTemplateService.queryPage(pageNum, pageSize);
        return page(page);
    }

    @Operation(summary = "保存Prompt模板")
    @PostMapping("/prompt")
    @SaCheckPermission("ai:prompt:update")
    public R<Void> savePrompt(@RequestBody AiPromptTemplate template) {
        aiPromptTemplateService.save(template);
        return success();
    }

    @Operation(summary = "删除Prompt模板")
    @DeleteMapping("/prompt/{id}")
    @SaCheckPermission("ai:prompt:update")
    public R<Void> deletePrompt(@PathVariable Long id) {
        aiPromptTemplateService.delete(id);
        return success();
    }

    @Operation(summary = "Prompt模板详情")
    @GetMapping("/prompt/{id}")
    public R<AiPromptTemplate> promptDetail(@PathVariable Long id) {
        return R.ok(aiPromptTemplateService.getById(id));
    }

    @Operation(summary = "更新Prompt模板")
    @PutMapping("/prompt/{id}")
    @SaCheckPermission("ai:prompt:update")
    public R<Void> updatePrompt(@PathVariable Long id, @RequestBody AiPromptTemplate template) {
        template.setId(id);
        aiPromptTemplateService.save(template);
        return success();
    }

    @Operation(summary = "激活Prompt模板")
    @PutMapping("/prompt/{id}/activate")
    @SaCheckPermission("ai:prompt:update")
    public R<Void> activatePrompt(@PathVariable Long id) {
        aiPromptTemplateService.activate(id);
        return success();
    }

    @Operation(summary = "Prompt模板版本列表")
    @GetMapping("/prompt/{id}/versions")
    public R<List<AiPromptTemplate>> promptVersions(@PathVariable Long id) {
        return R.ok(aiPromptTemplateService.getVersions(id));
    }

    @Operation(summary = "回滚Prompt模板")
    @PostMapping("/prompt/{id}/rollback")
    @SaCheckPermission("ai:prompt:update")
    public R<Void> rollbackPrompt(@PathVariable Long id) {
        aiPromptTemplateService.rollback(id);
        return success();
    }
}
