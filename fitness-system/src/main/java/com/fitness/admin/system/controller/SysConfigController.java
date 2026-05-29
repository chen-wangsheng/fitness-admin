package com.fitness.admin.system.controller;

import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.SysConfig;
import com.fitness.admin.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/sys-config")
@RequiredArgsConstructor
public class SysConfigController extends BaseController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "配置列表")
    @GetMapping("/list")
    public R<List<SysConfig>> list() {
        return success(sysConfigService.list());
    }

    @Operation(summary = "保存配置")
    @PostMapping
    public R<Void> save(@RequestBody SysConfig config) {
        sysConfigService.save(config);
        return success();
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysConfigService.delete(id);
        return success();
    }
}
