package com.fitness.admin.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.content.dto.PlanCreateDTO;
import com.fitness.admin.content.dto.PlanQueryDTO;
import com.fitness.admin.content.entity.PlanDay;
import com.fitness.admin.content.entity.PlanDayExercise;
import com.fitness.admin.content.entity.WorkoutPlan;
import com.fitness.admin.content.mapper.PlanDayExerciseMapper;
import com.fitness.admin.content.mapper.PlanDayMapper;
import com.fitness.admin.content.mapper.PlanMapper;
import com.fitness.admin.content.vo.PlanDayVO;
import com.fitness.admin.content.vo.PlanExerciseVO;
import com.fitness.admin.content.vo.PlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanMapper planMapper;
    private final PlanDayMapper planDayMapper;
    private final PlanDayExerciseMapper planDayExerciseMapper;

    public Page<WorkoutPlan> queryPage(PlanQueryDTO queryDTO) {
        Page<WorkoutPlan> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<WorkoutPlan> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(WorkoutPlan::getName, queryDTO.getKeyword());
        }
        if (StringUtils.hasText(queryDTO.getFitnessGoal())) {
            wrapper.eq(WorkoutPlan::getFitnessGoal, queryDTO.getFitnessGoal());
        }
        if (StringUtils.hasText(queryDTO.getDifficultyLevel())) {
            wrapper.eq(WorkoutPlan::getDifficultyLevel, queryDTO.getDifficultyLevel());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(WorkoutPlan::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getAiGenerated() != null) {
            wrapper.eq(WorkoutPlan::getAiGenerated, queryDTO.getAiGenerated());
        }

        wrapper.orderByDesc(WorkoutPlan::getCreatedAt);
        return planMapper.selectPage(page, wrapper);
    }

    public PlanVO getDetail(Long id) {
        WorkoutPlan plan = planMapper.selectById(id);
        if (plan == null) {
            return null;
        }
        PlanVO vo = new PlanVO();
        BeanUtils.copyProperties(plan, vo);

        List<PlanDay> days = planDayMapper.selectByPlanId(id);
        vo.setDays(days.stream().map(day -> {
            PlanDayVO dayVO = new PlanDayVO();
            BeanUtils.copyProperties(day, dayVO);
            List<PlanExerciseVO> exercises = planDayExerciseMapper.selectByPlanDayId(day.getId());
            dayVO.setExercises(exercises);
            return dayVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(PlanCreateDTO createDTO) {
        WorkoutPlan plan = new WorkoutPlan();
        BeanUtils.copyProperties(createDTO, plan);
        planMapper.insert(plan);

        if (createDTO.getDays() != null) {
            for (PlanCreateDTO.PlanDayDTO dayDTO : createDTO.getDays()) {
                PlanDay day = new PlanDay();
                day.setPlanId(plan.getId());
                day.setWeekNumber(dayDTO.getWeekNumber());
                day.setDayOfWeek(dayDTO.getDayNumber());
                day.setDayLabel(dayDTO.getFocus());
                day.setIsRestDay(dayDTO.getExercises() == null || dayDTO.getExercises().isEmpty() ? 1 : 0);
                planDayMapper.insert(day);

                if (dayDTO.getExercises() != null) {
                    for (int i = 0; i < dayDTO.getExercises().size(); i++) {
                        PlanCreateDTO.PlanExerciseDTO exDTO = dayDTO.getExercises().get(i);
                        PlanDayExercise exercise = new PlanDayExercise();
                        exercise.setPlanDayId(day.getId());
                        exercise.setExerciseId(exDTO.getExerciseId());
                        exercise.setSets(exDTO.getSets());
                        exercise.setReps(exDTO.getReps());
                        exercise.setDuration(exDTO.getDuration());
                        exercise.setRestSeconds(exDTO.getRestSeconds());
                        exercise.setSort(exDTO.getSort() != null ? exDTO.getSort() : i);
                        planDayExerciseMapper.insert(exercise);
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PlanCreateDTO updateDTO) {
        WorkoutPlan plan = new WorkoutPlan();
        BeanUtils.copyProperties(updateDTO, plan);
        plan.setId(id);
        planMapper.updateById(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        planMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(id);
        plan.setStatus(status);
        planMapper.updateById(plan);
    }
}
