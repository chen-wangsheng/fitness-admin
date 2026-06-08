package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiPromptTemplate;
import com.fitness.admin.ai.mapper.AiPromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPromptTemplateService {

    private final AiPromptTemplateMapper aiPromptTemplateMapper;

    public Page<AiPromptTemplate> queryPage(Integer pageNum, Integer pageSize) {
        Page<AiPromptTemplate> page = new Page<>(pageNum, pageSize);
        return aiPromptTemplateMapper.selectPage(page, null);
    }

    public AiPromptTemplate getById(Long id) {
        return aiPromptTemplateMapper.selectById(id);
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

    public void activate(Long id) {
        AiPromptTemplate template = aiPromptTemplateMapper.selectById(id);
        if (template != null) {
            template.setIsActive(1);
            template.setActivatedAt(LocalDateTime.now());
            aiPromptTemplateMapper.updateById(template);
        }
    }

    public List<AiPromptTemplate> getVersions(Long id) {
        AiPromptTemplate template = aiPromptTemplateMapper.selectById(id);
        if (template == null) {
            return Collections.emptyList();
        }
        // 当前简单实现：返回同templateKey的所有版本
        return aiPromptTemplateMapper.selectList(
                new LambdaQueryWrapper<AiPromptTemplate>()
                        .eq(AiPromptTemplate::getTemplateKey, template.getTemplateKey())
                        .orderByDesc(AiPromptTemplate::getVersion));
    }

    public void rollback(Long id) {
        AiPromptTemplate template = aiPromptTemplateMapper.selectById(id);
        if (template != null && template.getVersion() > 1) {
            // 简单实现：将版本号减1
            template.setVersion(template.getVersion() - 1);
            aiPromptTemplateMapper.updateById(template);
        }
    }
}
