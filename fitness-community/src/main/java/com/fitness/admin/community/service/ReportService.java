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

    /**
     * 处理举报。action: confirmed | dismissed, mapped to status 1 | 2.
     * reason 写入 handle_result;handlerId 写入 handler_id。
     */
    public void handle(Long id, String action, String reason, Long handlerId) {
        Integer status = switch (action == null ? "" : action) {
            case "confirmed" -> 1;
            case "dismissed" -> 2;
            default -> throw new IllegalArgumentException("action 仅支持 confirmed / dismissed");
        };
        Report report = new Report();
        report.setId(id);
        report.setStatus(status);
        report.setHandleResult(reason);
        report.setHandlerId(handlerId);
        reportMapper.updateById(report);
    }
}
