package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiPromptTemplate;
import com.fitness.admin.ai.mapper.AiPromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPromptTemplateService {

    private final AiPromptTemplateMapper aiPromptTemplateMapper;

    public Page<AiPromptTemplate> queryPage(Integer pageNum, Integer pageSize) {
        Page<AiPromptTemplate> page = new Page<>(pageNum, pageSize);
        return aiPromptTemplateMapper.selectPage(page, null);
    }

    public void save(AiPromptTemplate template) {
        if (template.getId() == null) {
            template.setVersion(1);
            aiPromptTemplateMapper.insert(template);
        } else {
            template.setVersion(template.getVersion() + 1);
            aiPromptTemplateMapper.updateById(template);
        }
    }

    public void delete(Long id) {
        aiPromptTemplateMapper.deleteById(id);
    }
}
