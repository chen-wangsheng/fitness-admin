package com.fitness.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.service.QiniuOssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@SaCheckLogin
public class UploadController {

    private final QiniuOssService qiniuOssService;

    @Operation(summary = "获取七牛云上传凭证")
    @GetMapping("/media")
    public R<Map<String, String>> getUploadCredential(@RequestParam String filename,
                                                      @RequestParam(defaultValue = "media") String dir) {
        Map<String, String> result = qiniuOssService.generateUploadCredential(filename, dir);
        return R.ok(result);
    }
}
