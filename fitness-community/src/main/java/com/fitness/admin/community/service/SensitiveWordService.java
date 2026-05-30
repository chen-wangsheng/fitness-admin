package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.community.entity.SensitiveWord;
import com.fitness.admin.community.mapper.SensitiveWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;

    public List<SensitiveWord> list() {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        return sensitiveWordMapper.selectList(wrapper);
    }

    public void save(SensitiveWord word) {
        if (word.getId() == null) {
            sensitiveWordMapper.insert(word);
        } else {
            sensitiveWordMapper.updateById(word);
        }
    }

    public void delete(Long id) {
        sensitiveWordMapper.deleteById(id);
    }
}
