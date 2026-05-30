package com.fitness.admin.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.user.dto.UserQueryDTO;
import com.fitness.admin.user.dto.UserUpdateDTO;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.entity.UserTag;
import com.fitness.admin.user.entity.UserTagRelation;
import com.fitness.admin.user.mapper.UserMapper;
import com.fitness.admin.user.mapper.UserTagMapper;
import com.fitness.admin.user.mapper.UserTagRelationMapper;
import com.fitness.admin.user.vo.UserVO;
import com.fitness.admin.user.vo.UserTagVO;
import com.fitness.admin.user.vo.UserDetailVO;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.enums.ResultCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserTagMapper userTagMapper;
    private final UserTagRelationMapper userTagRelationMapper;

    public Page<UserVO> queryUserPage(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(User::getNickname, queryDTO.getKeyword());
        }
        if (queryDTO.getGender() != null) {
            wrapper.eq(User::getGender, queryDTO.getGender());
        }
        if (StringUtils.hasText(queryDTO.getFitnessGoal())) {
            wrapper.eq(User::getFitnessGoal, queryDTO.getFitnessGoal());
        }
        if (StringUtils.hasText(queryDTO.getFitnessLevel())) {
            wrapper.eq(User::getFitnessLevel, queryDTO.getFitnessLevel());
        }

        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        List<UserVO> records = userPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        Page<UserVO> resultPage = new Page<>();
        BeanUtils.copyProperties(userPage, resultPage);
        resultPage.setRecords(records);
        return resultPage;
    }

    public UserDetailVO getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCodeEnum.DATA_NOT_FOUND);
        }
        UserDetailVO vo = new UserDetailVO();
        BeanUtils.copyProperties(convertToVO(user), vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO updateDTO) {
        User user = new User();
        user.setId(updateDTO.getId());
        user.setNickname(updateDTO.getNickname());
        user.setGender(updateDTO.getGender());
        user.setBirthday(updateDTO.getBirthday());
        user.setHeightCm(updateDTO.getHeightCm());
        user.setCurrentWeightKg(updateDTO.getCurrentWeightKg());
        user.setTargetWeightKg(updateDTO.getTargetWeightKg());
        user.setFitnessGoal(updateDTO.getFitnessGoal());
        user.setFitnessLevel(updateDTO.getFitnessLevel());
        user.setWorkoutDaysPerWeek(updateDTO.getWorkoutDaysPerWeek());
        user.setWorkoutDurationMin(updateDTO.getWorkoutDurationMin());
        userMapper.updateById(user);

        if (updateDTO.getTagIds() != null) {
            userTagRelationMapper.deleteByUserId(updateDTO.getId());
            for (Long tagId : updateDTO.getTagIds()) {
                UserTagRelation relation = new UserTagRelation();
                relation.setUserId(updateDTO.getId());
                relation.setTagId(tagId);
                userTagRelationMapper.insert(relation);
            }
        }
    }

    public void updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public void batchUpdateStatus(List<Long> userIds, Integer status) {
        for (Long userId : userIds) {
            updateStatus(userId, status);
        }
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        List<UserTag> tags = userMapper.selectTagsByUserId(user.getId());
        vo.setTags(tags.stream().map(tag -> {
            UserTagVO tagVO = new UserTagVO();
            tagVO.setId(tag.getId());
            tagVO.setName(tag.getName());
            tagVO.setColor(tag.getColor());
            return tagVO;
        }).collect(Collectors.toList()));
        return vo;
    }
}
