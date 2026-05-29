package com.fitness.admin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.content.entity.BodyPart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BodyPartMapper extends BaseMapper<BodyPart> {

    List<BodyPart> selectByExerciseId(Long exerciseId);
}
