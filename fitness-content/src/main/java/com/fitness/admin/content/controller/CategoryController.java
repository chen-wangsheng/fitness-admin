package com.fitness.admin.content.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.content.entity.ExerciseCategory;
import com.fitness.admin.content.service.CategoryService;
import com.fitness.admin.common.annotation.LogOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "动作分类管理")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@SaCheckPermission("category:read")
public class CategoryController extends BaseController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表")
    @GetMapping("/list")
    public R<List<ExerciseCategory>> list() {
        return success(categoryService.list());
    }

    @LogOperation(action = "新增", module = "分类管理")
    @Operation(summary = "保存分类")
    @PostMapping
    @SaCheckPermission("category:create")
    public R<Void> save(@RequestBody ExerciseCategory category) {
        categoryService.save(category);
        return success();
    }

    @LogOperation(action = "编辑", module = "分类管理")
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @SaCheckPermission("category:update")
    public R<Void> update(@PathVariable Long id, @RequestBody ExerciseCategory category) {
        category.setId(id);
        categoryService.save(category);
        return success();
    }

    @LogOperation(action = "删除", module = "分类管理")
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @SaCheckPermission("category:delete")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return success();
    }
}
