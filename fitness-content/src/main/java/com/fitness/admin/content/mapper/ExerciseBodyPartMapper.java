package com.fitness.admin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.content.entity.ExerciseBodyPart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExerciseBodyPartMapper extends BaseMapper<ExerciseBodyPart> {

    void deleteByExerciseId(Long exerciseId);
}
