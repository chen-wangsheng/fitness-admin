package com.fitness.admin.workout.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.workout.dto.BodyMetricQueryDTO;
import com.fitness.admin.workout.entity.BodyMetric;
import com.fitness.admin.workout.service.BodyMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "身体数据管理")
@RestController
@RequestMapping("/body-metric")
@RequiredArgsConstructor
@SaCheckPermission("body:read")
public class BodyMetricController extends BaseController {

    private final BodyMetricService bodyMetricService;

    @Operation(summary = "身体数据列表")
    @GetMapping("/list")
    public R<PageResult<BodyMetric>> list(BodyMetricQueryDTO queryDTO) {
        Page<BodyMetric> page = bodyMetricService.queryPage(queryDTO);
        return page((Page) page);
    }

    @Operation(summary = "保存身体数据")
    @PostMapping
    @SaCheckPermission("body:create")
    public R<Void> save(@RequestBody BodyMetric bodyMetric) {
        bodyMetricService.save(bodyMetric);
        return success();
    }

    @Operation(summary = "删除身体数据")
    @DeleteMapping("/{id}")
    @SaCheckPermission("body:delete")
    public R<Void> delete(@PathVariable Long id) {
        bodyMetricService.delete(id);
        return success();
    }
}
