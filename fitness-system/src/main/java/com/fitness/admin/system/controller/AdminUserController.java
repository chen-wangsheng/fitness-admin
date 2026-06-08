package com.fitness.admin.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.service.AdminUserService;
import com.fitness.admin.common.annotation.LogOperation;
import com.fitness.admin.user.entity.AdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理员管理")
@RestController
@RequestMapping("/admin-user")
@RequiredArgsConstructor
public class AdminUserController extends BaseController {

    private final AdminUserService adminUserService;

    @Operation(summary = "管理员列表")
    @GetMapping("/list")
    public R<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Map<String, Object>> result = adminUserService.queryPage(page, pageSize, keyword);
        return page((Page) result);
    }

    @LogOperation(action = "新增", module = "管理员管理")
    @Operation(summary = "新增管理员")
    @PostMapping
    public R<Void> create(@RequestBody AdminUser adminUser) {
        adminUserService.create(adminUser);
        return success();
    }

    @LogOperation(action = "编辑", module = "管理员管理")
    @Operation(summary = "更新管理员")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long roleId = body.get("roleId") != null ? Long.valueOf(body.get("roleId").toString()) : null;
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
        String nickname = (String) body.get("nickname");
        String avatar = (String) body.get("avatar");
        String email = (String) body.get("email");
        String phone = (String) body.get("phone");
        adminUserService.update(id, roleId, userId, nickname, avatar, email, phone);
        return success();
    }

    @LogOperation(action = "启用/禁用", module = "管理员管理")
    @Operation(summary = "更新管理员状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = Integer.valueOf(body.get("status").toString());
        adminUserService.updateStatus(id, status);
        return success();
    }

    @LogOperation(action = "重置密码", module = "管理员管理")
    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return success();
    }
}
