package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.community.entity.CommunityComment;
import com.fitness.admin.community.mapper.CommunityCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityCommentMapper communityCommentMapper;

    public Page<CommunityComment> queryPage(Integer pageNum, Integer pageSize) {
        Page<CommunityComment> page = new Page<>(pageNum, pageSize);
        return communityCommentMapper.selectPage(page, null);
    }

    public void updateStatus(Long id, Integer status) {
        CommunityComment comment = new CommunityComment();
        comment.setId(id);
        comment.setStatus(status);
        communityCommentMapper.updateById(comment);
    }
}
