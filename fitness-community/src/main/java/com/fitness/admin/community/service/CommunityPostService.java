package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.community.entity.CommunityPost;
import com.fitness.admin.community.mapper.CommunityPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostMapper communityPostMapper;

    public Page<CommunityPost> queryPage(Integer pageNum, Integer pageSize, Long userId, Integer status,
                                          LocalDateTime startDate, LocalDateTime endDate) {
        Page<CommunityPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(CommunityPost::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(CommunityPost::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(CommunityPost::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(CommunityPost::getCreatedAt, endDate);
        }
        wrapper.orderByDesc(CommunityPost::getCreatedAt);
        return communityPostMapper.selectPage(page, wrapper);
    }

    public CommunityPost getById(Long id) {
        return communityPostMapper.selectById(id);
    }

    public void updateStatus(Long id, Integer status) {
        CommunityPost post = new CommunityPost();
        post.setId(id);
        post.setStatus(status);
        communityPostMapper.updateById(post);
    }

    public void delete(Long id) {
        communityPostMapper.deleteById(id);
    }

    /**
     * 置顶/取消置顶。pinned=true 时记录 pinned_at,便于按置顶时间排序。
     */
    public void togglePin(Long id, boolean pinned) {
        CommunityPost update = new CommunityPost();
        update.setId(id);
        update.setPinned(pinned ? 1 : 0);
        update.setPinnedAt(pinned ? LocalDateTime.now() : null);
        communityPostMapper.updateById(update);
    }
}
