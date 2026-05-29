package com.fitness.admin.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.system.entity.OperationLog;
import com.fitness.admin.system.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public Page<OperationLog> queryPage(Integer pageNum, Integer pageSize) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        return operationLogMapper.selectPage(page, null);
    }
}
