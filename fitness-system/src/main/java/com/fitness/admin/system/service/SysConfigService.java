package com.fitness.admin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    public List<SysConfig> listByKeyPrefix(String keyPrefix) {
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
        wrapper.likeRight("config_key", keyPrefix);
        return sysConfigMapper.selectList(wrapper);
    }

    public void save(SysConfig config) {
        if (config.getId() == null) {
            sysConfigMapper.insert(config);
        } else {
            sysConfigMapper.updateById(config);
        }
    }

    public void saveByKey(String configKey, String configValue, String description) {
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("config_key", configKey);
        SysConfig existing = sysConfigMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setConfigValue(configValue);
            if (description != null) {
                existing.setDescription(description);
            }
            sysConfigMapper.updateById(existing);
        } else {
            SysConfig config = new SysConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setDescription(description);
            sysConfigMapper.insert(config);
        }
    }

    public void delete(Long id) {
        sysConfigMapper.deleteById(id);
    }
}
