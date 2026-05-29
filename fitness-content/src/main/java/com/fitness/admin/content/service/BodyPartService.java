package com.fitness.admin.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.content.entity.BodyPart;
import com.fitness.admin.content.mapper.BodyPartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BodyPartService {

    private final BodyPartMapper bodyPartMapper;

    public List<BodyPart> list() {
        LambdaQueryWrapper<BodyPart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BodyPart::getStatus, 1)
                .eq(BodyPart::getDeleted, 0)
                .orderByAsc(BodyPart::getSort);
        return bodyPartMapper.selectList(wrapper);
    }

    public void save(BodyPart bodyPart) {
        if (bodyPart.getId() == null) {
            bodyPartMapper.insert(bodyPart);
        } else {
            bodyPartMapper.updateById(bodyPart);
        }
    }

    public void delete(Long id) {
        bodyPartMapper.deleteById(id);
    }
}
