package com.fitness.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user")
public class AdminUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer status;
    private Long roleId;
    private String lastLoginIp;
    private java.time.LocalDateTime lastLoginTime;
}
