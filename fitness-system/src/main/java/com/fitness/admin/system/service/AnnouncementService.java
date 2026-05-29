package com.fitness.admin.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.system.entity.Announcement;
import com.fitness.admin.system.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public Page<Announcement> queryPage(Integer pageNum, Integer pageSize) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        return announcementMapper.selectPage(page, null);
    }

    public void save(Announcement announcement) {
        if (announcement.getId() == null) {
            announcementMapper.insert(announcement);
        } else {
            announcementMapper.updateById(announcement);
        }
    }

    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }
}
