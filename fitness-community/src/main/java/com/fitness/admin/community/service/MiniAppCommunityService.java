package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.exception.BizException;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.community.dto.*;
import com.fitness.admin.community.entity.CommunityComment;
import com.fitness.admin.community.entity.CommunityPost;
import com.fitness.admin.community.entity.PostLike;
import com.fitness.admin.community.mapper.CommunityCommentMapper;
import com.fitness.admin.community.mapper.CommunityPostMapper;
import com.fitness.admin.community.mapper.PostLikeMapper;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 小程序社区服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppCommunityService {

    private final CommunityPostMapper postMapper;
    private final CommunityCommentMapper commentMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;

    /**
     * 获取帖子列表
     */
    public PageResult<Map<String, Object>> getPostList(Integer pageNum, Integer pageSize) {
        Page<CommunityPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPost::getStatus, 1)
               .orderByDesc(CommunityPost::getCreatedAt);
        Page<CommunityPost> result = postMapper.selectPage(page, wrapper);

        return convertToPostPageResult(result);
    }

    /**
     * 获取我的帖子
     */
    public PageResult<Map<String, Object>> getMyPosts(Integer pageNum, Integer pageSize) {
        Long userId = getCurrentUserId();

        Page<CommunityPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPost::getUserId, userId)
               .orderByDesc(CommunityPost::getCreatedAt);
        Page<CommunityPost> result = postMapper.selectPage(page, wrapper);

        return convertToPostPageResult(result);
    }

    /**
     * 获取帖子详情
     */
    public PostDetailResponse getPostDetail(Long id) {
        CommunityPost post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException("帖子不存在");
        }

        User user = userMapper.selectById(post.getUserId());
        Long currentUserId = getCurrentUserId();

        PostDetailResponse response = new PostDetailResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setNickname(user != null ? user.getNickname() : "未知用户");
        response.setAvatarUrl(user != null ? user.getAvatarUrl() : null);
        response.setContent(post.getContent());
        response.setImages(parseImages(post.getImages()));
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCreatedAt(post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 检查当前用户是否已点赞
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getUserId, currentUserId)
                   .eq(PostLike::getPostId, id);
        Long likeCount = postLikeMapper.selectCount(likeWrapper);
        response.setIsLiked(likeCount > 0);

        return response;
    }

    /**
     * 发布帖子
     */
    @Transactional
    public void createPost(CreatePostRequest request) {
        Long userId = getCurrentUserId();

        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setContent(request.getContent());
        post.setImages(request.getImages() != null ? String.join(",", request.getImages()) : null);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.insert(post);

        log.info("用户{}发布帖子: postId={}", userId, post.getId());
    }

    /**
     * 点赞/取消点赞
     */
    @Transactional
    public LikeResponse toggleLike(Long postId) {
        Long userId = getCurrentUserId();

        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException("帖子不存在");
        }

        // 查询是否已点赞
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getUserId, userId)
               .eq(PostLike::getPostId, postId);
        PostLike existing = postLikeMapper.selectOne(wrapper);

        boolean liked;
        if (existing != null) {
            // 取消点赞
            postLikeMapper.deleteById(existing.getId());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            liked = false;
            log.info("用户{}取消点赞帖子: postId={}", userId, postId);
        } else {
            // 点赞
            PostLike like = new PostLike();
            like.setUserId(userId);
            like.setPostId(postId);
            like.setCreatedAt(LocalDateTime.now());
            postLikeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
            liked = true;
            log.info("用户{}点赞帖子: postId={}", userId, postId);
        }

        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);

        LikeResponse response = new LikeResponse();
        response.setLiked(liked);
        response.setLikeCount(post.getLikeCount());
        return response;
    }

    /**
     * 获取评论列表
     */
    public PageResult<Map<String, Object>> getComments(Long postId, Integer pageNum, Integer pageSize) {
        Page<CommunityComment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityComment::getPostId, postId)
               .eq(CommunityComment::getStatus, 1)
               .orderByAsc(CommunityComment::getCreatedAt);
        Page<CommunityComment> result = commentMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (CommunityComment comment : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("postId", comment.getPostId());
            map.put("userId", comment.getUserId());
            map.put("parentId", comment.getParentId());
            map.put("content", comment.getContent());
            map.put("likeCount", comment.getLikeCount());
            map.put("createdAt", comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            User user = userMapper.selectById(comment.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "未知用户");
            map.put("avatarUrl", user != null ? user.getAvatarUrl() : null);

            list.add(map);
        }

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(result.getTotal());
        pageResult.setPageNum(pageNum.longValue());
        pageResult.setPageSize(pageSize.longValue());
        pageResult.setPages((long) Math.ceil((double) result.getTotal() / pageSize));

        return pageResult;
    }

    /**
     * 发表评论
     */
    @Transactional
    public void createComment(Long postId, CreateCommentRequest request) {
        Long userId = getCurrentUserId();

        CommunityPost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException("帖子不存在");
        }

        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);

        log.info("用户{}评论帖子: postId={}, commentId={}", userId, postId, comment.getId());
    }

    /**
     * 转换为帖子页面结果
     */
    private PageResult<Map<String, Object>> convertToPostPageResult(Page<CommunityPost> page) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CommunityPost post : page.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("userId", post.getUserId());
            map.put("content", post.getContent());
            map.put("images", parseImages(post.getImages()));
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("createdAt", post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            User user = userMapper.selectById(post.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "未知用户");
            map.put("avatarUrl", user != null ? user.getAvatarUrl() : null);

            list.add(map);
        }

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setPages(page.getPages());

        return pageResult;
    }

    /**
     * 解析图片列表
     */
    private List<String> parseImages(String images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(images.split(","));
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }
        return userId;
    }
}
