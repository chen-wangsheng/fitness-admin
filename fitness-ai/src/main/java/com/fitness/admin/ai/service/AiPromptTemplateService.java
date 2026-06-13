package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiPromptTemplate;
import com.fitness.admin.ai.mapper.AiPromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPromptTemplateService {

    public static final String CACHE_NAME = "ai:prompt";

    private final AiPromptTemplateMapper aiPromptTemplateMapper;

    public Page<AiPromptTemplate> queryPage(Integer pageNum, Integer pageSize) {
        Page<AiPromptTemplate> page = new Page<>(pageNum, pageSize);
        return aiPromptTemplateMapper.selectPage(page, null);
    }

    @Cacheable(value = CACHE_NAME, key = "'id:' + #id")
    public AiPromptTemplate getById(Long id) {
        return aiPromptTemplateMapper.selectById(id);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void save(AiPromptTemplate template) {
        if (template.getId() == null) {
            template.setVersion(1);
            aiPromptTemplateMapper.insert(template);
        } else {
            template.setVersion(template.getVersion() + 1);
            aiPromptTemplateMapper.updateById(template);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Long id) {
        aiPromptTemplateMapper.deleteById(id);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
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
        // 当前简单实现:返回同 templateKey 的所有版本
        return aiPromptTemplateMapper.selectList(
                new LambdaQueryWrapper<AiPromptTemplate>()
                        .eq(AiPromptTemplate::getTemplateKey, template.getTemplateKey())
                        .orderByDesc(AiPromptTemplate::getVersion));
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void rollback(Long id) {
        AiPromptTemplate template = aiPromptTemplateMapper.selectById(id);
        if (template != null && template.getVersion() > 1) {
            // 简单实现:将版本号减1
            template.setVersion(template.getVersion() - 1);
            aiPromptTemplateMapper.updateById(template);
        }
    }
}
