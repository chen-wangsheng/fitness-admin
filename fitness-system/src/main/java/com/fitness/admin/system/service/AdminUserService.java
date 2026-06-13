package com.fitness.admin.system.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.system.dto.AdminUserUpsertDTO;
import com.fitness.admin.system.mapper.AdminUserEntityMapper;
import com.fitness.admin.user.entity.AdminUser;
import com.fitness.admin.user.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserEntityMapper adminUserEntityMapper;

    public Page<Map<String, Object>> queryPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return adminUserMapper.selectAdminPage(page, keyword);
    }

    public void create(AdminUserUpsertDTO dto) {
        AdminUser exist = adminUserMapper.selectByUsername(dto.getUsername());
        if (exist != null) {
            throw new BizException(ResultCodeEnum.USER_EXISTS);
        }
        AdminUser adminUser = adminUserEntityMapper.toEntity(dto);
        adminUser.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        adminUser.setStatus(1);
        adminUser.setDeleted(0);
        adminUserMapper.insert(adminUser);
    }

    public void update(Long id, AdminUserUpsertDTO dto) {
        AdminUser adminUser = adminUserEntityMapper.toEntity(dto);
        adminUser.setId(id);
        adminUserMapper.updateById(adminUser);
    }

    public void updateStatus(Long id, Integer status) {
        adminUserMapper.updateStatus(id, status);
    }

    public void resetPassword(Long id) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(id);
        adminUser.setPassword(DigestUtil.md5Hex("123456"));
        adminUserMapper.updateById(adminUser);
    }
}
