package com.fitness.admin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admin_login_log")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminUserId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private Integer loginStatus;
    private String failReason;
    private LocalDateTime createdAt;
}
