package com.fitness.admin.achievement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.achievement.entity.Checkin;
import com.fitness.admin.achievement.mapper.CheckinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinMapper checkinMapper;

    public Page<Checkin> queryPage(Integer pageNum, Integer pageSize) {
        Page<Checkin> page = new Page<>(pageNum, pageSize);
        return checkinMapper.selectPage(page, null);
    }
}
