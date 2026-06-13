package com.fitness.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.AdminRole;
import com.fitness.admin.system.service.AdminRoleService;
import com.fitness.admin.common.annotation.LogOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/admin-role")
@RequiredArgsConstructor
@SaCheckPermission("admin:role:read")
public class AdminRoleController extends BaseController {

    private final AdminRoleService adminRoleService;

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    public R<PageResult<AdminRole>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AdminRole> page = adminRoleService.queryPage(pageNum, pageSize);
        return page(page);
    }

    @LogOperation(action = "新增", module = "角色管理")
    @Operation(summary = "保存角色")
    @PostMapping
    @SaCheckPermission("admin:role:create")
    public R<Void> save(@RequestBody AdminRole role) {
        adminRoleService.save(role);
        return success();
    }

    @LogOperation(action = "删除", module = "角色管理")
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("admin:role:delete")
    public R<Void> delete(@PathVariable Long id) {
        adminRoleService.delete(id);
        return success();
    }
}
