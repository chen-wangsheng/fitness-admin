package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiSafetyRule;
import com.fitness.admin.ai.mapper.AiSafetyRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiSafetyRuleService {

    private final AiSafetyRuleMapper aiSafetyRuleMapper;

    public Page<AiSafetyRule> queryPage(Integer pageNum, Integer pageSize) {
        Page<AiSafetyRule> page = new Page<>(pageNum, pageSize);
        return aiSafetyRuleMapper.selectPage(page, null);
    }

    public void save(AiSafetyRule rule) {
        if (rule.getId() == null) {
            aiSafetyRuleMapper.insert(rule);
        } else {
            aiSafetyRuleMapper.updateById(rule);
        }
    }

    public void delete(Long id) {
        aiSafetyRuleMapper.deleteById(id);
    }
}
