package com.fitness.admin.system.service;

import com.fitness.admin.system.entity.SysConfig;
import com.fitness.admin.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public List<SysConfig> list() {
        return sysConfigMapper.selectList(null);
    }

    public void save(SysConfig config) {
        if (config.getId() == null) {
            sysConfigMapper.insert(config);
        } else {
            sysConfigMapper.updateById(config);
        }
    }

    public void delete(Long id) {
        sysConfigMapper.deleteById(id);
    }
}
