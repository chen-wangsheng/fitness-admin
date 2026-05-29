package com.fitness.admin.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.user.entity.UserTag;
import com.fitness.admin.user.mapper.UserTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTagService {

    private final UserTagMapper userTagMapper;

    public List<UserTag> list() {
        LambdaQueryWrapper<UserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTag::getStatus, 1)
                .eq(UserTag::getDeleted, 0)
                .orderByAsc(UserTag::getSort);
        return userTagMapper.selectList(wrapper);
    }

    public void save(UserTag tag) {
        if (tag.getId() == null) {
            userTagMapper.insert(tag);
        } else {
            userTagMapper.updateById(tag);
        }
    }

    public void delete(Long id) {
        userTagMapper.deleteById(id);
    }
}
