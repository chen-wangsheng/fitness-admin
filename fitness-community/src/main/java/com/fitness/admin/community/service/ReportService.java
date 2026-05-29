package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.community.entity.Report;
import com.fitness.admin.community.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;

    public Page<Report> queryPage(Integer pageNum, Integer pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        return reportMapper.selectPage(page, null);
    }

    public void handle(Long id, String result, Long handlerId) {
        Report report = new Report();
        report.setId(id);
        report.setStatus(1);
        report.setHandleResult(result);
        report.setHandlerId(handlerId);
        reportMapper.updateById(report);
    }
}
