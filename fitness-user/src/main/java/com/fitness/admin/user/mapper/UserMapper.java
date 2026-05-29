package com.fitness.admin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.entity.UserTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<UserTag> selectTagsByUserId(Long userId);
}
