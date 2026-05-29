package com.fitness.admin.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.OperationLog;
import com.fitness.admin.system.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/operation-log")
@RequiredArgsConstructor
public class OperationLogController extends BaseController {

    private final OperationLogService operationLogService;

    @Operation(summary = "日志列表")
    @GetMapping("/list")
    public R<PageResult<OperationLog>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<OperationLog> page = operationLogService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }
}
