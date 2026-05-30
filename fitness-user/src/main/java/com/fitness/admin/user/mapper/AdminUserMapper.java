package com.fitness.admin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.user.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    AdminUser selectByUsername(String username);

    Page<Map<String, Object>> selectAdminPage(Page<Map<String, Object>> page, @Param("keyword") String keyword);
}
