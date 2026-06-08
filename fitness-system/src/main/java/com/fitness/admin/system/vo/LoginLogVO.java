package com.fitness.admin.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogVO {

    private Long id;
    private String username;
    private LocalDateTime loginTime;
    private String ip;
    private String userAgent;
    private String status;
}
