package com.fitness.admin.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.system.entity.AdminRole;
import com.fitness.admin.system.mapper.AdminRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminRoleMapper adminRoleMapper;

    public Page<AdminRole> queryPage(Integer pageNum, Integer pageSize) {
        Page<AdminRole> page = new Page<>(pageNum, pageSize);
        Page<AdminRole> result = adminRoleMapper.selectPage(page, null);
        // 列表接口同样把 JSON 解析成数组给前端
        if (result != null && result.getRecords() != null) {
            result.getRecords().forEach(AdminRole::refreshPermissionsFromRaw);
        }
        return result;
    }

    public AdminRole getById(Long id) {
        AdminRole role = adminRoleMapper.selectById(id);
        if (role != null) {
            // 数据库存的是 JSON 数组串,响应给前端时拆成数组
            role.refreshPermissionsFromRaw();
        }
        return role;
    }

    /**
     * 保存角色并把 permissions 数组写入 permissions 列(JSON 数组串)。
     * 入参 DTO 字段类型为 List<String>(transient 不会入库),
     * 这里手工序列化为 JSON 后写到 admin_role.permissions 列。
     */
    public void saveWithPermissions(AdminRole role) {
        role.writePermissionsAsRaw();

        if (role.getId() == null) {
            adminRoleMapper.insert(role);
        } else {
            adminRoleMapper.updateById(role);
        }
    }

    public List<String> parsePermissions(AdminRole role) {
        if (role == null) {
            return new ArrayList<>();
        }
        List<String> perms = role.getPermissions();
        if (perms == null) {
            return new ArrayList<>();
        }
        return perms;
    }

    public void delete(Long id) {
        adminRoleMapper.deleteById(id);
    }
}
