package com.fitness.admin.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.common.enums.ResultCodeEnum;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.content.dto.*;
import com.fitness.admin.content.entity.Exercise;
import com.fitness.admin.content.entity.ExerciseFavorite;
import com.fitness.admin.content.mapper.ExerciseFavoriteMapper;
import com.fitness.admin.content.mapper.ExerciseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 小程序动作服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppExerciseService {

    private final ExerciseFavoriteMapper exerciseFavoriteMapper;
    private final ExerciseMapper exerciseMapper;

    /**
     * 检查收藏状态
     */
    public FavoriteCheckResponse checkFavorite(Long exerciseId) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<ExerciseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExerciseFavorite::getUserId, userId)
               .eq(ExerciseFavorite::getExerciseId, exerciseId);
        Long count = exerciseFavoriteMapper.selectCount(wrapper);

        FavoriteCheckResponse response = new FavoriteCheckResponse();
        response.setIsFavorited(count > 0);
        return response;
    }

    /**
     * 收藏/取消收藏
     */
    @Transactional
    public FavoriteToggleResponse toggleFavorite(Long exerciseId) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<ExerciseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExerciseFavorite::getUserId, userId)
               .eq(ExerciseFavorite::getExerciseId, exerciseId);
        ExerciseFavorite existing = exerciseFavoriteMapper.selectOne(wrapper);

        boolean isFavorited;
        if (existing != null) {
            exerciseFavoriteMapper.deleteById(existing.getId());
            isFavorited = false;
            log.info("用户{}取消收藏动作: exerciseId={}", userId, exerciseId);
        } else {
            ExerciseFavorite favorite = new ExerciseFavorite();
            favorite.setUserId(userId);
            favorite.setExerciseId(exerciseId);
            favorite.setCreatedAt(LocalDateTime.now());
            exerciseFavoriteMapper.insert(favorite);
            isFavorited = true;
            log.info("用户{}收藏动作: exerciseId={}", userId, exerciseId);
        }

        FavoriteToggleResponse response = new FavoriteToggleResponse();
        response.setIsFavorited(isFavorited);
        return response;
    }

    /**
     * 获取收藏列表
     */
    public List<FavoriteItem> getFavoriteList() {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<ExerciseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExerciseFavorite::getUserId, userId)
               .orderByDesc(ExerciseFavorite::getCreatedAt);
        List<ExerciseFavorite> favorites = exerciseFavoriteMapper.selectList(wrapper);

        List<FavoriteItem> list = new ArrayList<>();
        for (ExerciseFavorite fav : favorites) {
            Exercise exercise = exerciseMapper.selectById(fav.getExerciseId());
            if (exercise != null) {
                FavoriteItem item = new FavoriteItem();
                item.setId(fav.getId());
                item.setExerciseId(exercise.getId());
                item.setExerciseName(exercise.getName());
                item.setExerciseNameEn(exercise.getNameEn());
                item.setDifficulty(exercise.getDifficulty());
                item.setExerciseType(exercise.getExerciseType());
                item.setEquipment(exercise.getEquipment());
                item.setDemoImageUrl(exercise.getDemoImageUrl());
                item.setIsCompound(exercise.getIsCompound());
                list.add(item);
            }
        }

        return list;
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
}
