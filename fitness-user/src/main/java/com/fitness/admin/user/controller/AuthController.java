package com.fitness.admin.user.controller;

import com.fitness.admin.common.annotation.LogOperation;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.user.dto.ChangePasswordRequest;
import com.fitness.admin.user.dto.LoginRequest;
import com.fitness.admin.user.dto.LoginResponse;
import com.fitness.admin.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @LogOperation(action = "登录", module = "认证管理")
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return success(authService.login(request));
    }

    @LogOperation(action = "登出", module = "认证管理")
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return success();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public R<LoginResponse> refreshToken() {
        return success(authService.refreshToken());
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public R<LoginResponse.UserInfo> profile() {
        return success(authService.getCurrentUserInfo());
    }

    @LogOperation(action = "修改密码", module = "认证管理")
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getOldPassword(), request.getNewPassword());
        return success();
    }
}
