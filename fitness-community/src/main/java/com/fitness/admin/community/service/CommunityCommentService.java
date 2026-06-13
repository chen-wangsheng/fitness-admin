package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.community.entity.CommunityComment;
import com.fitness.admin.community.mapper.CommunityCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityCommentMapper communityCommentMapper;

    public Page<CommunityComment> queryPage(Integer pageNum, Integer pageSize, Long postId, Long userId,
                                              LocalDateTime startDate, LocalDateTime endDate) {
        Page<CommunityComment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        if (postId != null) {
            wrapper.eq(CommunityComment::getPostId, postId);
        }
        if (userId != null) {
            wrapper.eq(CommunityComment::getUserId, userId);
        }
        if (startDate != null) {
            wrapper.ge(CommunityComment::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(CommunityComment::getCreatedAt, endDate);
        }
        wrapper.orderByDesc(CommunityComment::getCreatedAt);
        return communityCommentMapper.selectPage(page, wrapper);
    }

    public void updateStatus(Long id, Integer status) {
        CommunityComment comment = new CommunityComment();
        comment.setId(id);
        comment.setStatus(status);
        communityCommentMapper.updateById(comment);
    }

    public void delete(Long id) {
        communityCommentMapper.deleteById(id);
    }
}
