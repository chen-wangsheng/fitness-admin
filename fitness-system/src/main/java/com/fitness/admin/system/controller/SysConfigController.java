package com.fitness.admin.system.controller;

import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.SysConfig;
import com.fitness.admin.system.service.SysConfigService;
import com.fitness.admin.common.annotation.LogOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/sys-config")
@RequiredArgsConstructor
public class SysConfigController extends BaseController {

    private final SysConfigService sysConfigService;

    private static final String AI_CONFIG_PREFIX = "ai.";

    @Operation(summary = "配置列表")
    @GetMapping("/list")
    public R<List<SysConfig>> list() {
        return success(sysConfigService.list());
    }

    @LogOperation(action = "新增", module = "系统配置")
    @Operation(summary = "保存配置")
    @PostMapping
    public R<Void> save(@RequestBody SysConfig config) {
        sysConfigService.save(config);
        return success();
    }

    @LogOperation(action = "编辑", module = "系统配置")
    @Operation(summary = "按key更新配置")
    @PutMapping("/{configKey}")
    public R<Void> updateByKey(@PathVariable String configKey, @RequestBody Map<String, String> body) {
        String configValue = body.get("configValue");
        String description = body.get("description");
        sysConfigService.saveByKey(configKey, configValue, description);
        return success();
    }

    @LogOperation(action = "删除", module = "系统配置")
    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysConfigService.delete(id);
        return success();
    }

    @Operation(summary = "获取AI配置")
    @GetMapping("/ai-config")
    public R<Map<String, String>> getAiConfig() {
        List<SysConfig> configs = sysConfigService.listByKeyPrefix(AI_CONFIG_PREFIX);
        Map<String, String> result = new LinkedHashMap<>();
        for (SysConfig config : configs) {
            // 去掉前缀 "ai." 返回给前端
            String key = config.getConfigKey();
            if (key.startsWith(AI_CONFIG_PREFIX)) {
                key = key.substring(AI_CONFIG_PREFIX.length());
            }
            result.put(key, config.getConfigValue());
        }
        return success(result);
    }

    @LogOperation(action = "编辑", module = "系统配置")
    @Operation(summary = "更新AI配置")
    @PutMapping("/ai-config")
    public R<Void> updateAiConfig(@RequestBody Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            String fullKey = AI_CONFIG_PREFIX + entry.getKey();
            sysConfigService.saveByKey(fullKey, entry.getValue(), null);
        }
        return success();
    }

    @Operation(summary = "测试AI连接")
    @PostMapping("/ai-config/test-connection")
    public R<Void> testAiConnection() {
        // TODO: 实际测试LLM连接
        return success();
    }
}
