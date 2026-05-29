package com.fitness.admin.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.community.entity.CommunityComment;
import com.fitness.admin.community.service.CommunityCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论管理")
@RestController
@RequestMapping("/community-comment")
@RequiredArgsConstructor
public class CommunityCommentController extends BaseController {

    private final CommunityCommentService communityCommentService;

    @Operation(summary = "评论列表")
    @GetMapping("/list")
    public R<PageResult<CommunityComment>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CommunityComment> page = communityCommentService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "更新状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        communityCommentService.updateStatus(id, status);
        return success();
    }
}
