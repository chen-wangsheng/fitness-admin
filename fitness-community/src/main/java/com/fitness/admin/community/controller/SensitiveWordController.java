package com.fitness.admin.community.controller;

import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.community.entity.SensitiveWord;
import com.fitness.admin.community.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "敏感词管理")
@RestController
@RequestMapping("/sensitive-word")
@RequiredArgsConstructor
public class SensitiveWordController extends BaseController {

    private final SensitiveWordService sensitiveWordService;

    @Operation(summary = "敏感词列表")
    @GetMapping("/list")
    public R<List<SensitiveWord>> list() {
        return success(sensitiveWordService.list());
    }

    @Operation(summary = "保存敏感词")
    @PostMapping
    public R<Void> save(@RequestBody SensitiveWord word) {
        sensitiveWordService.save(word);
        return success();
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return success();
    }
}
