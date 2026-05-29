package com.fitness.admin.workout.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.workout.dto.BodyMetricQueryDTO;
import com.fitness.admin.workout.entity.BodyMetric;
import com.fitness.admin.workout.mapper.BodyMetricMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BodyMetricService {

    private final BodyMetricMapper bodyMetricMapper;

    public Page<BodyMetric> queryPage(BodyMetricQueryDTO queryDTO) {
        Page<BodyMetric> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<BodyMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BodyMetric::getDeleted, 0);

        if (queryDTO.getUserId() != null) {
            wrapper.eq(BodyMetric::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(BodyMetric::getRecordDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(BodyMetric::getRecordDate, queryDTO.getEndDate());
        }

        wrapper.orderByDesc(BodyMetric::getRecordDate);
        return bodyMetricMapper.selectPage(page, wrapper);
    }

    public void save(BodyMetric bodyMetric) {
        if (bodyMetric.getId() == null) {
            bodyMetricMapper.insert(bodyMetric);
        } else {
            bodyMetricMapper.updateById(bodyMetric);
        }
    }

    public void delete(Long id) {
        bodyMetricMapper.deleteById(id);
    }
}
