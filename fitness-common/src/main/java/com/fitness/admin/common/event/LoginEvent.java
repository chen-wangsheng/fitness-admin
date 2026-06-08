package com.fitness.admin.common.event;

import lombok.Data;

@Data
public class LoginEvent {
    private Long adminUserId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private Integer loginStatus;
    private String failReason;

    public LoginEvent(Long adminUserId, String username, String ipAddress,
                      String userAgent, Integer loginStatus, String failReason) {
        this.adminUserId = adminUserId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginStatus = loginStatus;
        this.failReason = failReason;
    }
}
