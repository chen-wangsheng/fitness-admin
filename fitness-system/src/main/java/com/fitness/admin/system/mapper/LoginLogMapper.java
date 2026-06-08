package com.fitness.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.system.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    Page<LoginLog> selectPageWithUsername(Page<LoginLog> page);
}
