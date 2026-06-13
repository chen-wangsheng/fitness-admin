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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        // 直接从 User 实体复制（保留 openid 等字段）
        BeanUtils.copyProperties(user, vo);
        vo.setStatusCode(user.getStatus());

        // 加载标签
        List<UserTag> tags = userMapper.selectTagsByUserId(user.getId());
        vo.setTags(tags.stream().map(tag -> {
            UserTagVO tagVO = new UserTagVO();
            tagVO.setId(tag.getId());
            tagVO.setName(tag.getName());
            tagVO.setColor(tag.getColor());
            return tagVO;
        }).collect(Collectors.toList()));

        // 计算 BMI = 体重(kg) / 身高(m)²
        if (user.getCurrentWeightKg() != null && user.getHeightCm() != null
                && user.getHeightCm().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = user.getHeightCm()
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal bmi = user.getCurrentWeightKg()
                    .divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
            vo.setBmi(bmi);
        }

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
        vo.setStatusCode(user.getStatus());
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
