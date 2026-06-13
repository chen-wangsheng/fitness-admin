package com.fitness.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.Announcement;
import com.fitness.admin.system.service.MiniAppAnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序公告接口
 */
@Tag(name = "小程序-公告模块")
@RestController
@RequestMapping("/miniapp/announcement")
@RequiredArgsConstructor
@SaCheckLogin
public class MiniAppAnnouncementController extends BaseController {

    private final MiniAppAnnouncementService miniAppAnnouncementService;

    @Operation(summary = "最新公告")
    @GetMapping("/latest")
    public R<Announcement> getLatest() {
        return success(miniAppAnnouncementService.getLatest());
    }
}
