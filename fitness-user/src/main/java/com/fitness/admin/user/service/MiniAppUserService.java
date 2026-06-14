package com.fitness.admin.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.user.dto.MiniAppLoginResponse;
import com.fitness.admin.user.dto.MiniAppUserInfo;
import com.fitness.admin.user.dto.UpdateProfileRequest;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.entity.UserFeedback;
import com.fitness.admin.user.entity.UserFitnessProfile;
import com.fitness.admin.user.mapper.UserFeedbackMapper;
import com.fitness.admin.user.mapper.UserFitnessProfileMapper;
import com.fitness.admin.user.mapper.UserMapper;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 小程序用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppUserService {

    private final UserMapper userMapper;
    private final UserFitnessProfileMapper userFitnessProfileMapper;
    private final UserFeedbackMapper userFeedbackMapper;
    private final WxMaService wxMaService;

    /**
     * 微信登录
     */
    public MiniAppLoginResponse wxLogin(String code) {
        // 1. 用code换取openid
        String openid = getOpenidByCode(code);

        // 2. 查询或创建用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户" + openid.substring(openid.length() - 6));
            user.setGender(0);
            user.setStatus(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("新用户注册: userId={}, openid={}", user.getId(), openid);
        }

        // 3. 检查用户状态
        if (user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }

        // 4. 使用Sa-Token登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        // 5. 构建响应
        MiniAppLoginResponse response = new MiniAppLoginResponse();
        response.setToken(token);
        response.setRefreshToken(token); // Sa-Token暂无独立refreshToken，使用相同token

        MiniAppUserInfo userInfo = new MiniAppUserInfo();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setGender(user.getGender());
        userInfo.setFitnessGoal(user.getFitnessGoal());
        userInfo.setFitnessLevel(user.getFitnessLevel());
        userInfo.setCurrentPlanId(user.getCurrentPlanId());
        userInfo.setStatus(user.getStatus());
        response.setUserInfo(userInfo);

        return response;
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /**
     * 更新用户信息
     */
    public void updateProfile(UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(LocalDate.parse(request.getBirthday()));
        }
        if (request.getHeightCm() != null) {
            user.setHeightCm(request.getHeightCm());
        }
        if (request.getCurrentWeightKg() != null) {
            user.setCurrentWeightKg(request.getCurrentWeightKg());
        }
        if (request.getTargetWeightKg() != null) {
            user.setTargetWeightKg(request.getTargetWeightKg());
        }
        if (request.getFitnessGoal() != null) {
            user.setFitnessGoal(request.getFitnessGoal());
        }
        if (request.getFitnessLevel() != null) {
            user.setFitnessLevel(request.getFitnessLevel());
        }
        if (request.getWorkoutDaysPerWeek() != null) {
            user.setWorkoutDaysPerWeek(request.getWorkoutDaysPerWeek());
        }
        if (request.getWorkoutDurationMin() != null) {
            user.setWorkoutDurationMin(request.getWorkoutDurationMin());
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            user.setEmail(email.isEmpty() ? null : email);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 获取健身画像
     */
    public UserFitnessProfile getFitnessProfile() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<UserFitnessProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFitnessProfile::getUserId, userId);
        return userFitnessProfileMapper.selectOne(wrapper);
    }

    /**
     * 更新健身画像
     */
    public void updateFitnessProfile(UserFitnessProfile profile) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<UserFitnessProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFitnessProfile::getUserId, userId);
        UserFitnessProfile existing = userFitnessProfileMapper.selectOne(wrapper);

        if (existing == null) {
            profile.setUserId(userId);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            userFitnessProfileMapper.insert(profile);
        } else {
            profile.setId(existing.getId());
            profile.setUserId(userId);
            profile.setUpdatedAt(LocalDateTime.now());
            userFitnessProfileMapper.updateById(profile);
        }
    }

    /**
     * 提交意见反馈
     */
    public void submitFeedback(String content) {
        Long userId = getCurrentUserId();
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setContent(content);
        feedback.setCreatedAt(LocalDateTime.now());
        userFeedbackMapper.insert(feedback);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userId;
    }

    /**
     * 通过微信code换取openid
     */
    private String getOpenidByCode(String code) {
        try {
            WxMaJscode2SessionResult result = wxMaService.getUserService().getSessionInfo(code);
            return result.getOpenid();
        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            throw new BizException("微信登录失败: " + e.getError().getErrorMsg());
        }
    }
}
