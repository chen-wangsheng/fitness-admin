package com.fitness.admin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.system.entity.Announcement;
import com.fitness.admin.system.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 小程序公告服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppAnnouncementService {

    private final AnnouncementMapper announcementMapper;

    /**
     * 获取最新公告
     */
    public Announcement getLatest() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
               .orderByDesc(Announcement::getCreatedAt)
               .last("LIMIT 1");
        return announcementMapper.selectOne(wrapper);
    }
}
