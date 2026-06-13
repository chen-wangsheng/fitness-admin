package com.fitness.admin.community.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.community.entity.SensitiveWord;
import com.fitness.admin.community.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "敏感词管理")
@RestController
@RequestMapping("/sensitive-word")
@RequiredArgsConstructor
@SaCheckPermission("sensitive:read")
public class SensitiveWordController extends BaseController {

    private final SensitiveWordService sensitiveWordService;

    @Operation(summary = "敏感词列表")
    @GetMapping("/list")
    public R<PageResult<SensitiveWord>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return page(sensitiveWordService.listPage(pageNum, pageSize));
    }

    @Operation(summary = "保存敏感词")
    @PostMapping
    @SaCheckPermission("sensitive:create")
    public R<Void> save(@RequestBody SensitiveWord word) {
        sensitiveWordService.save(word);
        return success();
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/{id}")
    @SaCheckPermission("sensitive:delete")
    public R<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return success();
    }
}
