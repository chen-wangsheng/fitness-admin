package com.fitness.admin.community.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.common.result.R;
import com.fitness.admin.community.service.UserMuteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序用户管理（社区相关）。
 * 当前只暴露禁言入口，禁言后评论/发帖会被拦截（待业务层接入）。
 */
@Tag(name = "小程序用户管理")
@RestController
@RequestMapping("/community-user")
@RequiredArgsConstructor
@SaCheckPermission("community:update")
public class CommunityUserController extends BaseController {

    private final UserMuteService userMuteService;

    @Operation(summary = "禁言用户")
    @PostMapping("/{id}/mute")
    @SaCheckPermission("community:update")
    public R<Void> mute(@PathVariable("id") Long userId, @RequestBody Map<String, Object> body) {
        Integer duration = toInt(body.get("duration"));
        String reason = body.get("reason") == null ? null : String.valueOf(body.get("reason"));
        Long operatorId = SecurityUtil.getCurrentUserId();
        userMuteService.mute(userId, duration, reason, operatorId);
        return success();
    }

    private static Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.valueOf(value.toString());
    }
}
