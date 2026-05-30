package com.fitness.admin.user.service;

import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.user.dto.LoginRequest;
import com.fitness.admin.user.dto.LoginResponse;
import com.fitness.admin.user.entity.AdminUser;
import com.fitness.admin.user.mapper.AdminUserMapper;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserMapper adminUserMapper;

    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BizException(ResultCodeEnum.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BizException(ResultCodeEnum.USER_DISABLED);
        }
        String encryptPassword = DigestUtil.md5Hex(request.getPassword());
        if (!encryptPassword.equals(user.getPassword())) {
            throw new BizException(ResultCodeEnum.USER_PASSWORD_ERROR);
        }

        StpUtil.login(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        response.setExpire(StpUtil.getTokenTimeout());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        response.setUserInfo(userInfo);

        return response;
    }

    public void logout() {
        StpUtil.logout();
    }

    public LoginResponse refreshToken() {
        StpUtil.renewTimeout(7200);
        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        response.setExpire(StpUtil.getTokenTimeout());
        return response;
    }

    public void changePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCodeEnum.USER_NOT_FOUND);
        }
        String encryptOld = DigestUtil.md5Hex(oldPassword);
        if (!encryptOld.equals(user.getPassword())) {
            throw new BizException(ResultCodeEnum.USER_PASSWORD_ERROR);
        }
        AdminUser update = new AdminUser();
        update.setId(userId);
        update.setPassword(DigestUtil.md5Hex(newPassword));
        adminUserMapper.updateById(update);
    }

    public LoginResponse.UserInfo getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCodeEnum.USER_NOT_FOUND);
        }

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        
        // 超级管理员拥有所有权限
        userInfo.setPermissions(List.of("*"));
        
        return userInfo;
    }
}
