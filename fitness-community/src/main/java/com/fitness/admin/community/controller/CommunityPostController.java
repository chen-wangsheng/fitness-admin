package com.fitness.admin.community.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.community.entity.CommunityPost;
import com.fitness.admin.community.service.CommunityPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "社区动态管理")
@RestController
@RequestMapping("/community-post")
@RequiredArgsConstructor
@SaCheckPermission("community:read")
public class CommunityPostController extends BaseController {

    private final CommunityPostService communityPostService;

    @Operation(summary = "动态列表")
    @GetMapping("/list")
    public R<PageResult<CommunityPost>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) Long userId,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        Page<CommunityPost> page = communityPostService.queryPage(pageNum, pageSize, userId, status, startDate, endDate);
        return page(page);
    }

    @Operation(summary = "动态详情")
    @GetMapping("/{id}")
    public R<CommunityPost> detail(@PathVariable Long id) {
        return success(communityPostService.getById(id));
    }

    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission("community:update")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        communityPostService.updateStatus(id, body.get("status"));
        return success();
    }

    @Operation(summary = "删除动态")
    @DeleteMapping("/{id}")
    @SaCheckPermission("community:delete")
    public R<Void> delete(@PathVariable Long id) {
        communityPostService.delete(id);
        return success();
    }

    @Operation(summary = "置顶/取消置顶")
    @PutMapping("/{id}/pin")
    @SaCheckPermission("community:update")
    public R<Void> togglePin(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body) {
        communityPostService.togglePin(id, Boolean.TRUE.equals(body.get("pinned")));
        return success();
    }
}
