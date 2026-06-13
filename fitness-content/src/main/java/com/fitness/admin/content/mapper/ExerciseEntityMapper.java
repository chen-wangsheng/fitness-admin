package com.fitness.admin.content.mapper;

import com.fitness.admin.content.dto.ExerciseCreateDTO;
import com.fitness.admin.content.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * 动作 DTO ↔ Entity 转换。
 *
 * <p>DTO 携带了 {@code bodyPartIds}(关联表字段)与
 * {@code status} 等审计字段,显式 ignore,避免请求体覆盖。
 * 关联表的写入由 service 单独处理,不在 mapper 范围内。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExerciseEntityMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true),
    })
    Exercise toEntity(ExerciseCreateDTO dto);
}
