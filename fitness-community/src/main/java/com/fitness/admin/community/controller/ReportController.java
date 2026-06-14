package com.fitness.admin.community.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.community.entity.Report;
import com.fitness.admin.community.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "举报管理")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@SaCheckPermission("report:read")
public class ReportController extends BaseController {

    private final ReportService reportService;

    @Operation(summary = "举报列表")
    @GetMapping("/list")
    public R<PageResult<Report>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Report> page = reportService.queryPage(pageNum, pageSize);
        return page(page);
    }

    /**
     * 处理举报。前端传 action: confirmed | dismissed, reason: 处理说明(可选)。
     * handlerId 从当前登录管理员自动取,不再让前端传,避免越权。
     */
    @Operation(summary = "处理举报")
    @PutMapping("/{id}/handle")
    @SaCheckPermission("report:update")
    public R<Void> handle(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.get("action");
        String reason = body.get("reason");
        Long handlerId = SecurityUtil.getCurrentUserId();
        reportService.handle(id, action, reason, handlerId);
        return success();
    }
}
