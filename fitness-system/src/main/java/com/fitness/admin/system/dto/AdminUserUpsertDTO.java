package com.fitness.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员创建/编辑入参 DTO。
 *
 * <p>与 {@link com.fitness.admin.user.entity.AdminUser} 区分,只暴露业务可写字段,
 * 通过 {@link com.fitness.admin.system.mapper.AdminUserEntityMapper} 转 entity。
 * 显式忽略 id / password(由 service 加密) / status / deleted / lastLogin* 字段,
 * 防止 controller 入参覆盖篡改。
 */
@Data
public class AdminUserUpsertDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64)
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64)
    private String nickname;

    /** 仅 create 时必填,update 时为空表示不修改密码。 */
    private String password;

    private String avatar;
    private String email;
    private String phone;
    private Long roleId;
    private Long userId;
}
