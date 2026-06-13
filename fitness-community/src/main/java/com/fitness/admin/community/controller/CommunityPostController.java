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
import org.springframework.web.bind.annotation.*;

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
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CommunityPost> page = communityPostService.queryPage(pageNum, pageSize);
        return page(page);
    }

    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission("community:update")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        communityPostService.updateStatus(id, body.get("status"));
        return success();
    }
}
