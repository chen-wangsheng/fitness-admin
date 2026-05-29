package com.fitness.admin.content.controller;

import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.content.entity.BodyPart;
import com.fitness.admin.content.service.BodyPartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "身体部位管理")
@RestController
@RequestMapping("/body-part")
@RequiredArgsConstructor
public class BodyPartController extends BaseController {

    private final BodyPartService bodyPartService;

    @Operation(summary = "部位列表")
    @GetMapping("/list")
    public R<List<BodyPart>> list() {
        return success(bodyPartService.list());
    }

    @Operation(summary = "保存部位")
    @PostMapping
    public R<Void> save(@RequestBody BodyPart bodyPart) {
        bodyPartService.save(bodyPart);
        return success();
    }

    @Operation(summary = "删除部位")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        bodyPartService.delete(id);
        return success();
    }
}
