package com.fitness.admin.achievement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.achievement.entity.Achievement;
import com.fitness.admin.achievement.service.AchievementService;
import com.fitness.admin.common.annotation.LogOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "成就管理")
@RestController
@RequestMapping("/achievement")
@RequiredArgsConstructor
public class AchievementController extends BaseController {

    private final AchievementService achievementService;

    @Operation(summary = "成就列表")
    @GetMapping("/list")
    public R<PageResult<Achievement>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Achievement> page = achievementService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "成就详情")
    @GetMapping("/{id}")
    public R<Achievement> detail(@PathVariable Long id) {
        return R.ok(achievementService.getById(id));
    }

    @LogOperation(action = "新增", module = "成就管理")
    @Operation(summary = "保存成就")
    @PostMapping
    public R<Void> save(@RequestBody Achievement achievement) {
        achievementService.save(achievement);
        return success();
    }

    @LogOperation(action = "编辑", module = "成就管理")
    @Operation(summary = "更新成就")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Achievement achievement) {
        achievement.setId(id);
        achievementService.save(achievement);
        return success();
    }

    @LogOperation(action = "删除", module = "成就管理")
    @Operation(summary = "删除成就")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        achievementService.delete(id);
        return success();
    }
}
