package com.fitness.admin.system.controller;

import com.fitness.admin.common.result.R;
import com.fitness.admin.system.service.CosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CosService cosService;

    @Operation(summary = "获取COS预签名上传URL")
    @GetMapping("/media")
    public R<Map<String, String>> getUploadUrl(@RequestParam String filename,
                                                @RequestParam(defaultValue = "media") String dir) {
        String[] urls = cosService.generatePresignedUrl(filename, dir);

        Map<String, String> result = new HashMap<>();
        result.put("uploadUrl", urls[0]);
        result.put("fileUrl", urls[1]);
        return R.ok(result);
    }
}
