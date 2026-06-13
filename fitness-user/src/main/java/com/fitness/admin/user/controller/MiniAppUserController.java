package com.fitness.admin.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.user.dto.FeedbackRequest;
import com.fitness.admin.user.dto.MiniAppLoginResponse;
import com.fitness.admin.user.dto.UpdateProfileRequest;
import com.fitness.admin.user.dto.WxLoginRequest;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.entity.UserFitnessProfile;
import com.fitness.admin.user.service.MiniAppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序用户端接口
 */
@Tag(name = "小程序-用户模块")
@RestController
@RequestMapping("/miniapp/user")
@RequiredArgsConstructor
public class MiniAppUserController extends BaseController {

    private final MiniAppUserService miniAppUserService;

    @Operation(summary = "微信登录")
    @PostMapping("/login")
    @SaIgnore
    public R<MiniAppLoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return success(miniAppUserService.wxLogin(request.getCode()));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    @SaCheckLogin
    public R<User> getProfile() {
        return success(miniAppUserService.getCurrentUser());
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    @SaCheckLogin
    public R<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        miniAppUserService.updateProfile(request);
        return success();
    }

    @Operation(summary = "获取健身画像")
    @GetMapping("/fitness-profile")
    @SaCheckLogin
    public R<UserFitnessProfile> getFitnessProfile() {
        return success(miniAppUserService.getFitnessProfile());
    }

    @Operation(summary = "更新健身画像")
    @PutMapping("/fitness-profile")
    @SaCheckLogin
    public R<Void> updateFitnessProfile(@RequestBody UserFitnessProfile profile) {
        miniAppUserService.updateFitnessProfile(profile);
        return success();
    }

    @Operation(summary = "意见反馈")
    @PostMapping("/feedback")
    @SaCheckLogin
    public R<Void> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        miniAppUserService.submitFeedback(request.getContent());
        return success();
    }
}
