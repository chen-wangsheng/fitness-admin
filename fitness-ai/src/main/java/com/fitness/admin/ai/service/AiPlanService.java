package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.mapper.AiPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPlanService {

    private final AiPlanMapper aiPlanMapper;

    public Page<AiPlan> queryPage(Integer pageNum, Integer pageSize) {
        Page<AiPlan> page = new Page<>(pageNum, pageSize);
        return aiPlanMapper.selectPage(page, null);
    }

    public void updateStatus(Long id, Integer status) {
        AiPlan plan = new AiPlan();
        plan.setId(id);
        plan.setStatus(status);
        aiPlanMapper.updateById(plan);
    }
}
