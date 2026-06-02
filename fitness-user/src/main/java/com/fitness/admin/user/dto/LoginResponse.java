package com.fitness.admin.user.dto;

import lombok.Data;
import java.util.List;

/**
 * 管理员登录响应
 */
@Data
public class LoginResponse {

    private String token;
    private Long expire;
    private String refreshToken;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private List<String> permissions;
    }
}
