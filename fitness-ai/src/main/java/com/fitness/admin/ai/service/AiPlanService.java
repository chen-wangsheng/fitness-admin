package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiAdjustmentConfig;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.entity.PlanLoadAdjustment;
import com.fitness.admin.ai.mapper.AiAdjustmentConfigMapper;
import com.fitness.admin.ai.mapper.AiPlanMapper;
import com.fitness.admin.ai.mapper.PlanLoadAdjustmentMapper;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.content.entity.WorkoutPlan;
import com.fitness.admin.content.mapper.PlanMapper;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPlanService {

    private final AiPlanMapper aiPlanMapper;
    private final PlanMapper planMapper;
    private final PlanLoadAdjustmentMapper planLoadAdjustmentMapper;
    private final AiAdjustmentConfigMapper aiAdjustmentConfigMapper;
    private final UserMapper userMapper;

    public Page<AiPlan> queryPage(Integer pageNum, Integer pageSize) {
        Page<AiPlan> page = new Page<>(pageNum, pageSize);
        return aiPlanMapper.selectPage(page, null);
    }

    public void updateStatus(Long id, String status) {
        AiPlan plan = new AiPlan();
        plan.setId(id);
        plan.setStatus(status);
        aiPlanMapper.updateById(plan);
    }

    @Transactional
    public Long convertToSystem(Long id) {
        AiPlan aiPlan = aiPlanMapper.selectById(id);
        if (aiPlan == null) {
            throw new RuntimeException("AI计划不存在");
        }

        WorkoutPlan workoutPlan = new WorkoutPlan();
        workoutPlan.setName("AI生成-" + (aiPlan.getGoal() != null ? aiPlan.getGoal() : "训练计划"));
        workoutPlan.setDescription(aiPlan.getExplanation());
        workoutPlan.setFitnessGoal(aiPlan.getGoal());
        workoutPlan.setDaysPerWeek(aiPlan.getDaysPerWeek());
        workoutPlan.setDurationWeeks(4);
        workoutPlan.setDifficultyLevel("intermediate");
        workoutPlan.setIsSystem(1);
        workoutPlan.setAiGenerated(1);
        workoutPlan.setAiGenerationParams(aiPlan.getGenerationParams());
        workoutPlan.setStatus(1);
        planMapper.insert(workoutPlan);

        AiPlan update = new AiPlan();
        update.setId(id);
        update.setConverted(1);
        update.setConvertedPlanId(workoutPlan.getId());
        update.setStatus("confirmed");
        aiPlanMapper.updateById(update);

        // 更新用户的当前活跃计划
        Long userId = aiPlan.getUserId();
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setCurrentPlanId(workoutPlan.getId());
                userMapper.updateById(user);
            }
        }

        return workoutPlan.getId();
    }

    public AiPlan getDetail(Long id) {
        return aiPlanMapper.selectById(id);
    }

    public List<PlanLoadAdjustment> getAdjustments(Long aiPlanId) {
        return planLoadAdjustmentMapper.selectList(
                new LambdaQueryWrapper<PlanLoadAdjustment>()
                        .eq(PlanLoadAdjustment::getAiPlanId, aiPlanId)
                        .orderByAsc(PlanLoadAdjustment::getWeekNumber));
    }

    public List<AiAdjustmentConfig> getAdjustmentRules() {
        return aiAdjustmentConfigMapper.selectList(null);
    }
}
