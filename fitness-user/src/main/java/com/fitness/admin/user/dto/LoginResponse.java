package com.fitness.admin.user.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private Long expire;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }
}
