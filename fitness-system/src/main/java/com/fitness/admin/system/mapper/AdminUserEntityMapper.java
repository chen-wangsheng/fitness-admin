package com.fitness.admin.system.mapper;

import com.fitness.admin.system.dto.AdminUserUpsertDTO;
import com.fitness.admin.user.entity.AdminUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * 管理员 DTO ↔ Entity 转换。
 *
 * <p>关键点:
 * <ul>
 *   <li>显式 ignore 掉 id / status / deleted / lastLoginIp / lastLoginTime,避免请求体覆盖</li>
 *   <li>password 不在 mapper 中处理,由 service 层加密后写入</li>
 *   <li>{@code updateEntity} 用于 in-place 更新,保留 id 字段</li>
 * </ul>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserEntityMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "lastLoginIp", ignore = true),
            @Mapping(target = "lastLoginTime", ignore = true),
    })
    AdminUser toEntity(AdminUserUpsertDTO dto);

    @Mappings({
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "lastLoginIp", ignore = true),
            @Mapping(target = "lastLoginTime", ignore = true),
    })
    void updateEntity(AdminUserUpsertDTO dto, @MappingTarget AdminUser entity);
}
