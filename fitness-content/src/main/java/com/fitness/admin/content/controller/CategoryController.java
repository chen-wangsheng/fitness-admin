package com.fitness.admin.content.controller;

import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.content.entity.ExerciseCategory;
import com.fitness.admin.content.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "动作分类管理")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController extends BaseController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表")
    @GetMapping("/list")
    public R<List<ExerciseCategory>> list() {
        return success(categoryService.list());
    }

    @Operation(summary = "保存分类")
    @PostMapping
    public R<Void> save(@RequestBody ExerciseCategory category) {
        categoryService.save(category);
        return success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return success();
    }
}
