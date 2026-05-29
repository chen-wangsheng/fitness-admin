package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.community.entity.CommunityPost;
import com.fitness.admin.community.mapper.CommunityPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostMapper communityPostMapper;

    public Page<CommunityPost> queryPage(Integer pageNum, Integer pageSize) {
        Page<CommunityPost> page = new Page<>(pageNum, pageSize);
        return communityPostMapper.selectPage(page, null);
    }

    public void updateStatus(Long id, Integer status) {
        CommunityPost post = new CommunityPost();
        post.setId(id);
        post.setStatus(status);
        communityPostMapper.updateById(post);
    }
}
