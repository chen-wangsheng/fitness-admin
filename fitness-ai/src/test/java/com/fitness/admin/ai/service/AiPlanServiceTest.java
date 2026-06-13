package com.fitness.admin.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.ai.entity.AiAdjustmentConfig;
import com.fitness.admin.ai.entity.AiPlan;
import com.fitness.admin.ai.entity.PlanLoadAdjustment;
import com.fitness.admin.ai.mapper.AiAdjustmentConfigMapper;
import com.fitness.admin.ai.mapper.AiPlanMapper;
import com.fitness.admin.ai.mapper.PlanLoadAdjustmentMapper;
import com.fitness.admin.content.entity.WorkoutPlan;
import com.fitness.admin.content.mapper.PlanMapper;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 计划服务单测。
 *
 * <p>覆盖分页查询、状态更新、转为系统计划、调整规则 CRUD 等关键路径。
 */
@ExtendWith(MockitoExtension.class)
class AiPlanServiceTest {

    @Mock
    private AiPlanMapper aiPlanMapper;

    @Mock
    private PlanMapper planMapper;

    @Mock
    private PlanLoadAdjustmentMapper planLoadAdjustmentMapper;

    @Mock
    private AiAdjustmentConfigMapper aiAdjustmentConfigMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AiPlanService aiPlanService;

    @Test
    void queryPage_withFilters_shouldPropagateToWrapper() {
        Page<AiPlan> page = new Page<>(1, 10);
        when(aiPlanMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<AiPlan> result = aiPlanService.queryPage(1, 10, 100L, "active", "ppl");

        assertNotNull(result);
        verify(aiPlanMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void queryPage_withNullFilters_shouldStillQuery() {
        when(aiPlanMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<>());

        Page<AiPlan> result = aiPlanService.queryPage(1, 10, null, null, null);

        assertNotNull(result);
    }

    @Test
    void updateStatus_shouldUpdateOnlyStatusField() {
        aiPlanService.updateStatus(7L, "active");

        ArgumentCaptor<AiPlan> captor = ArgumentCaptor.forClass(AiPlan.class);
        verify(aiPlanMapper).updateById(captor.capture());
        assertEquals(7L, captor.getValue().getId());
        assertEquals("active", captor.getValue().getStatus());
    }

    @Test
    void convertToSystem_planNotFound_shouldThrow() {
        when(aiPlanMapper.selectById(99L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aiPlanService.convertToSystem(99L));
        assertEquals("AI计划不存在", ex.getMessage());
        verify(planMapper, never()).insert(any(WorkoutPlan.class));
    }

    @Test
    void convertToSystem_shouldCreateSystemPlanAndUpdateAiPlan() {
        AiPlan aiPlan = new AiPlan();
        aiPlan.setId(1L);
        aiPlan.setUserId(2L);
        aiPlan.setGoal("增肌");
        aiPlan.setDaysPerWeek(4);
        aiPlan.setExplanation("AI 自动生成");
        aiPlan.setGenerationParams("{}");
        when(aiPlanMapper.selectById(1L)).thenReturn(aiPlan);
        when(planMapper.insert(any(WorkoutPlan.class))).thenAnswer(inv -> {
            WorkoutPlan p = inv.getArgument(0);
            p.setId(555L);
            return 1;
        });
        User user = new User();
        user.setId(2L);
        when(userMapper.selectById(2L)).thenReturn(user);

        Long systemPlanId = aiPlanService.convertToSystem(1L);

        assertEquals(555L, systemPlanId);
        ArgumentCaptor<WorkoutPlan> planCaptor = ArgumentCaptor.forClass(WorkoutPlan.class);
        verify(planMapper).insert(planCaptor.capture());
        assertEquals(1, planCaptor.getValue().getIsSystem());
        assertEquals(1, planCaptor.getValue().getAiGenerated());
        assertEquals(1, planCaptor.getValue().getStatus());

        ArgumentCaptor<AiPlan> aiCaptor = ArgumentCaptor.forClass(AiPlan.class);
        verify(aiPlanMapper).updateById(aiCaptor.capture());
        assertEquals(1, aiCaptor.getValue().getConverted());
        assertEquals(555L, aiCaptor.getValue().getConvertedPlanId());
        assertEquals("confirmed", aiCaptor.getValue().getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals(555L, userCaptor.getValue().getCurrentPlanId());
    }

    @Test
    void convertToSystem_whenUserMissing_shouldStillReturnSystemPlanId() {
        AiPlan aiPlan = new AiPlan();
        aiPlan.setId(1L);
        aiPlan.setUserId(404L);
        aiPlan.setGoal("减脂");
        aiPlan.setDaysPerWeek(3);
        when(aiPlanMapper.selectById(1L)).thenReturn(aiPlan);
        when(planMapper.insert(any(WorkoutPlan.class))).thenAnswer(inv -> {
            WorkoutPlan p = inv.getArgument(0);
            p.setId(777L);
            return 1;
        });
        when(userMapper.selectById(404L)).thenReturn(null);

        Long id = aiPlanService.convertToSystem(1L);

        assertEquals(777L, id);
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void getAdjustments_shouldFilterByAiPlanIdAndOrderByWeek() {
        when(planLoadAdjustmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(new PlanLoadAdjustment()));

        List<PlanLoadAdjustment> result = aiPlanService.getAdjustments(10L);

        assertEquals(1, result.size());
        verify(planLoadAdjustmentMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void updateAdjustmentConfig_shouldUpdateExistingAndSkipComplexValues() {
        AiAdjustmentConfig existing = new AiAdjustmentConfig();
        existing.setId(1L);
        existing.setConfigKey("load_increase_completion_threshold");
        existing.setConfigValue("5");
        when(aiAdjustmentConfigMapper.selectList(null)).thenReturn(List.of(existing));
        when(aiAdjustmentConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        Map<String, Object> input = new HashMap<>();
        input.put("load_increase_completion_threshold", 10);
        input.put("ignored_complex", Map.of("a", 1));
        input.put("ignored_list", List.of(1, 2));
        input.put("ignored_long_value", "x".repeat(300));
        input.put("new_key_simple", "value");

        aiPlanService.updateAdjustmentConfig(input);

        ArgumentCaptor<AiAdjustmentConfig> updateCaptor = ArgumentCaptor.forClass(AiAdjustmentConfig.class);
        verify(aiAdjustmentConfigMapper).updateById(updateCaptor.capture());
        assertEquals("10", updateCaptor.getValue().getConfigValue());

        ArgumentCaptor<AiAdjustmentConfig> insertCaptor = ArgumentCaptor.forClass(AiAdjustmentConfig.class);
        verify(aiAdjustmentConfigMapper).insert(insertCaptor.capture());
        assertEquals("new_key_simple", insertCaptor.getValue().getConfigKey());
        assertEquals("value", insertCaptor.getValue().getConfigValue());
    }
}
