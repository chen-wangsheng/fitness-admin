package com.fitness.admin.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.content.entity.ExerciseCategory;
import com.fitness.admin.content.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    public static final String CACHE_NAME = "dict";
    public static final String CACHE_KEY = "'exerciseCategory:all'";

    private final CategoryMapper categoryMapper;

    @Cacheable(value = CACHE_NAME, key = CACHE_KEY)
    public List<ExerciseCategory> list() {
        LambdaQueryWrapper<ExerciseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ExerciseCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void save(ExerciseCategory category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
