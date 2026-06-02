package com.fitness.admin.user.dto;

import lombok.Data;

/**
 * 小程序登录响应
 */
@Data
public class MiniAppLoginResponse {
    private String token;
    private String refreshToken;
    private MiniAppUserInfo userInfo;
}
