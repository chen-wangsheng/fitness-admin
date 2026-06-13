package com.fitness.admin.content.mapper;

import com.fitness.admin.content.dto.PlanCreateDTO;
import com.fitness.admin.content.entity.PlanDay;
import com.fitness.admin.content.entity.PlanDayExercise;
import com.fitness.admin.content.entity.WorkoutPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * 训练计划相关 DTO <-> Entity 转换。
 *
 * <p>关键点:
 * <ul>
 *   <li>PlanCreateDTO 与 WorkoutPlan 字段名不同(例如 {@code coverImageUrl} vs 实体类 {@code coverImageUrl},
 *       {@code difficulty} vs {@code difficultyLevel}),显式 {@code @Mapping} 列出</li>
 *   <li>DB 自增 / 审计字段 {@code id}/{@code createdAt}/{@code updatedAt}/{@code createdBy}
 *       显式 {@code ignore = true},避免 controller 入参覆盖篡改</li>
 *   <li>{@code days} 是 List<PlanDayDTO>,递归到 PlanDay</li>
 * </ul>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanEntityMapper {

    @Mappings({
            @Mapping(source = "difficulty", target = "difficultyLevel"),
    })
    WorkoutPlan toEntity(PlanCreateDTO dto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    PlanDay toEntity(PlanCreateDTO.PlanDayDTO dto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    PlanDayExercise toEntity(PlanCreateDTO.PlanExerciseDTO dto);
}
