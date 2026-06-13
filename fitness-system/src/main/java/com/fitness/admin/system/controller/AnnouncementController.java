package com.fitness.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.Announcement;
import com.fitness.admin.system.service.AnnouncementService;
import com.fitness.admin.common.annotation.LogOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "公告管理")
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
@SaCheckPermission("announcement:read")
public class AnnouncementController extends BaseController {

    private final AnnouncementService announcementService;

    @Operation(summary = "公告列表")
    @GetMapping("/list")
    public R<PageResult<Announcement>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Announcement> page = announcementService.queryPage(pageNum, pageSize);
        return page(page);
    }

    @LogOperation(action = "新增", module = "公告管理")
    @Operation(summary = "保存公告")
    @PostMapping
    @SaCheckPermission("announcement:create")
    public R<Void> save(@RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return success();
    }

    @LogOperation(action = "编辑", module = "公告管理")
    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    @SaCheckPermission("announcement:update")
    public R<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.save(announcement);
        return success();
    }

    @LogOperation(action = "删除", module = "公告管理")
    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    @SaCheckPermission("announcement:delete")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return success();
    }
}
