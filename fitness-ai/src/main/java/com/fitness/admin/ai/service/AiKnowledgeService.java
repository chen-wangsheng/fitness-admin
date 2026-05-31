package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.KnowledgeBase;
import com.fitness.admin.ai.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiKnowledgeService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public Page<KnowledgeBase> queryPage(Integer pageNum, Integer pageSize) {
        Page<KnowledgeBase> page = new Page<>(pageNum, pageSize);
        return knowledgeBaseMapper.selectPage(page, null);
    }

    public void save(KnowledgeBase knowledgeBase) {
        if (knowledgeBase.getId() == null) {
            knowledgeBase.setVectorStatus("pending");
            knowledgeBaseMapper.insert(knowledgeBase);
        } else {
            knowledgeBaseMapper.updateById(knowledgeBase);
        }
    }

    public KnowledgeBase getDetail(Long id) {
        return knowledgeBaseMapper.selectById(id);
    }

    public void delete(Long id) {
        knowledgeBaseMapper.deleteById(id);
    }
}
