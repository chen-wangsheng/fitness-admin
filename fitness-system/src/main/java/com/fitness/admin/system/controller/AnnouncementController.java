package com.fitness.admin.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.Announcement;
import com.fitness.admin.system.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "公告管理")
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController extends BaseController {

    private final AnnouncementService announcementService;

    @Operation(summary = "公告列表")
    @GetMapping("/list")
    public R<PageResult<Announcement>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Announcement> page = announcementService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "保存公告")
    @PostMapping
    public R<Void> save(@RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return success();
    }
}
