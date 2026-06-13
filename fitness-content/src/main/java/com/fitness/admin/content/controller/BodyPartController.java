package com.fitness.admin.content.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.content.entity.BodyPart;
import com.fitness.admin.content.service.BodyPartService;
import com.fitness.admin.common.annotation.LogOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "身体部位管理")
@RestController
@RequestMapping("/body-part")
@RequiredArgsConstructor
@SaCheckPermission("body-part:read")
public class BodyPartController extends BaseController {

    private final BodyPartService bodyPartService;

    @Operation(summary = "部位列表")
    @GetMapping("/list")
    public R<List<BodyPart>> list() {
        return success(bodyPartService.list());
    }

    @LogOperation(action = "新增", module = "部位管理")
    @Operation(summary = "保存部位")
    @PostMapping
    @SaCheckPermission("body-part:create")
    public R<Void> save(@RequestBody BodyPart bodyPart) {
        bodyPartService.save(bodyPart);
        return success();
    }

    @LogOperation(action = "编辑", module = "部位管理")
    @Operation(summary = "更新部位")
    @PutMapping("/{id}")
    @SaCheckPermission("body-part:update")
    public R<Void> update(@PathVariable Long id, @RequestBody BodyPart bodyPart) {
        bodyPart.setId(id);
        bodyPartService.save(bodyPart);
        return success();
    }

    @LogOperation(action = "删除", module = "部位管理")
    @Operation(summary = "删除部位")
    @DeleteMapping("/{id}")
    @SaCheckPermission("body-part:delete")
    public R<Void> delete(@PathVariable Long id) {
        bodyPartService.delete(id);
        return success();
    }
}
