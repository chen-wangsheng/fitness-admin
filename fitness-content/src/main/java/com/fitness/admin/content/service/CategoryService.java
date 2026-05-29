package com.fitness.admin.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.content.entity.ExerciseCategory;
import com.fitness.admin.content.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<ExerciseCategory> list() {
        LambdaQueryWrapper<ExerciseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExerciseCategory::getStatus, 1)
                .eq(ExerciseCategory::getDeleted, 0)
                .orderByAsc(ExerciseCategory::getSort);
        return categoryMapper.selectList(wrapper);
    }

    public void save(ExerciseCategory category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
