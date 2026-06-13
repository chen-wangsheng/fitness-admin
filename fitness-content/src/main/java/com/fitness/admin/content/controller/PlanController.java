package com.fitness.admin.content.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.content.dto.PlanCreateDTO;
import com.fitness.admin.content.dto.PlanQueryDTO;
import com.fitness.admin.content.entity.WorkoutPlan;
import com.fitness.admin.content.service.PlanService;
import com.fitness.admin.content.vo.PlanVO;
import com.fitness.admin.common.annotation.LogOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "训练计划管理")
@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
@SaCheckPermission("plan:read")
public class PlanController extends BaseController {

    private final PlanService planService;

    @Operation(summary = "计划列表")
    @GetMapping("/list")
    public R<PageResult<WorkoutPlan>> list(PlanQueryDTO queryDTO) {
        Page<WorkoutPlan> page = planService.queryPage(queryDTO);
        return page((Page) page);
    }

    @Operation(summary = "计划详情")
    @GetMapping("/{id}")
    public R<PlanVO> getById(@PathVariable Long id) {
        return success(planService.getDetail(id));
    }

    @LogOperation(action = "新增", module = "训练计划")
    @Operation(summary = "创建计划")
    @PostMapping
    @SaCheckPermission("plan:create")
    public R<Void> create(@RequestBody PlanCreateDTO createDTO) {
        planService.create(createDTO);
        return success();
    }

    @LogOperation(action = "编辑", module = "训练计划")
    @Operation(summary = "更新计划")
    @PutMapping("/{id}")
    @SaCheckPermission("plan:update")
    public R<Void> update(@PathVariable Long id, @RequestBody PlanCreateDTO updateDTO) {
        planService.update(id, updateDTO);
        return success();
    }

    @LogOperation(action = "删除", module = "训练计划")
    @Operation(summary = "删除计划")
    @DeleteMapping("/{id}")
    @SaCheckPermission("plan:delete")
    public R<Void> delete(@PathVariable Long id) {
        planService.delete(id);
        return success();
    }

    @LogOperation(action = "编辑", module = "训练计划")
    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission("plan:update")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        planService.updateStatus(id, body.get("status"));
        return success();
    }
}
