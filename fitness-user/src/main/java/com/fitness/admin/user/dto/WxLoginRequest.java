package com.fitness.admin.user.dto;

import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class WxLoginRequest {
    /** 微信登录code */
    private String code;
}
