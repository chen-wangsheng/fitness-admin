package com.fitness.admin.achievement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.achievement.entity.Achievement;
import com.fitness.admin.achievement.mapper.AchievementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;

    public Page<Achievement> queryPage(Integer pageNum, Integer pageSize) {
        Page<Achievement> page = new Page<>(pageNum, pageSize);
        return achievementMapper.selectPage(page, null);
    }

    public Achievement getById(Long id) {
        return achievementMapper.selectById(id);
    }

    public void save(Achievement achievement) {
        if (achievement.getId() == null) {
            achievementMapper.insert(achievement);
        } else {
            achievementMapper.updateById(achievement);
        }
    }

    public void delete(Long id) {
        achievementMapper.deleteById(id);
    }
}
