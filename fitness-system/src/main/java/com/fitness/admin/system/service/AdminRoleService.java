package com.fitness.admin.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.system.entity.AdminRole;
import com.fitness.admin.system.mapper.AdminRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminRoleMapper adminRoleMapper;

    public Page<AdminRole> queryPage(Integer pageNum, Integer pageSize) {
        Page<AdminRole> page = new Page<>(pageNum, pageSize);
        return adminRoleMapper.selectPage(page, null);
    }

    public void save(AdminRole role) {
        if (role.getId() == null) {
            adminRoleMapper.insert(role);
        } else {
            adminRoleMapper.updateById(role);
        }
    }

    public void delete(Long id) {
        adminRoleMapper.deleteById(id);
    }
}
