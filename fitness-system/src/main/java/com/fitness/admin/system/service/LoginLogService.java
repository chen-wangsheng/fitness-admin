package com.fitness.admin.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.system.entity.LoginLog;
import com.fitness.admin.system.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogMapper loginLogMapper;

    public Page<LoginLog> queryPage(Integer pageNum, Integer pageSize) {
        Page<LoginLog> page = new Page<>(pageNum, pageSize);
        return loginLogMapper.selectPageWithUsername(page);
    }
}
