package com.fitness.admin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.user.entity.UserTagRelation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTagRelationMapper extends BaseMapper<UserTagRelation> {

    void deleteByUserId(Long userId);
}
