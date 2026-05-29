package com.fitness.admin.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.content.dto.ExerciseCreateDTO;
import com.fitness.admin.content.dto.ExerciseQueryDTO;
import com.fitness.admin.content.entity.BodyPart;
import com.fitness.admin.content.entity.Exercise;
import com.fitness.admin.content.entity.ExerciseBodyPart;
import com.fitness.admin.content.mapper.BodyPartMapper;
import com.fitness.admin.content.mapper.ExerciseBodyPartMapper;
import com.fitness.admin.content.mapper.ExerciseMapper;
import com.fitness.admin.content.vo.BodyPartVO;
import com.fitness.admin.content.vo.ExerciseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseMapper exerciseMapper;
    private final BodyPartMapper bodyPartMapper;
    private final ExerciseBodyPartMapper exerciseBodyPartMapper;

    public Page<ExerciseVO> queryPage(ExerciseQueryDTO queryDTO) {
        Page<Exercise> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Exercise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exercise::getDeleted, 0);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(Exercise::getName, queryDTO.getKeyword());
        }
        if (StringUtils.hasText(queryDTO.getExerciseType())) {
            wrapper.eq(Exercise::getExerciseType, queryDTO.getExerciseType());
        }
        if (StringUtils.hasText(queryDTO.getEquipment())) {
            wrapper.eq(Exercise::getEquipment, queryDTO.getEquipment());
        }
        if (StringUtils.hasText(queryDTO.getDifficulty())) {
            wrapper.eq(Exercise::getDifficulty, queryDTO.getDifficulty());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Exercise::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Exercise::getCreatedAt);
        Page<Exercise> exercisePage = exerciseMapper.selectPage(page, wrapper);

        List<ExerciseVO> records = exercisePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        Page<ExerciseVO> resultPage = new Page<>();
        BeanUtils.copyProperties(exercisePage, resultPage);
        resultPage.setRecords(records);
        return resultPage;
    }

    public ExerciseVO getDetail(Long id) {
        Exercise exercise = exerciseMapper.selectById(id);
        if (exercise == null) {
            return null;
        }
        return convertToVO(exercise);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(ExerciseCreateDTO createDTO) {
        Exercise exercise = new Exercise();
        BeanUtils.copyProperties(createDTO, exercise);
        exerciseMapper.insert(exercise);

        if (createDTO.getBodyPartIds() != null) {
            for (Long bodyPartId : createDTO.getBodyPartIds()) {
                ExerciseBodyPart ebp = new ExerciseBodyPart();
                ebp.setExerciseId(exercise.getId());
                ebp.setBodyPartId(bodyPartId);
                exerciseBodyPartMapper.insert(ebp);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ExerciseCreateDTO updateDTO) {
        Exercise exercise = new Exercise();
        BeanUtils.copyProperties(updateDTO, exercise);
        exercise.setId(id);
        exerciseMapper.updateById(exercise);

        if (updateDTO.getBodyPartIds() != null) {
            exerciseBodyPartMapper.deleteByExerciseId(id);
            for (Long bodyPartId : updateDTO.getBodyPartIds()) {
                ExerciseBodyPart ebp = new ExerciseBodyPart();
                ebp.setExerciseId(id);
                ebp.setBodyPartId(bodyPartId);
                exerciseBodyPartMapper.insert(ebp);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        exerciseMapper.deleteById(id);
    }

    private ExerciseVO convertToVO(Exercise exercise) {
        ExerciseVO vo = new ExerciseVO();
        BeanUtils.copyProperties(exercise, vo);
        List<BodyPart> bodyParts = bodyPartMapper.selectByExerciseId(exercise.getId());
        vo.setBodyParts(bodyParts.stream().map(bp -> {
            BodyPartVO bpVO = new BodyPartVO();
            bpVO.setId(bp.getId());
            bpVO.setName(bp.getName());
            bpVO.setIcon(bp.getIcon());
            return bpVO;
        }).collect(Collectors.toList()));
        return vo;
    }
}
