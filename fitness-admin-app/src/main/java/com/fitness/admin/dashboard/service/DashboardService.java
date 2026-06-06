package com.fitness.admin.dashboard.service;

import com.fitness.admin.dashboard.vo.DashboardOverviewVO;

/**
 * 数据看板服务
 */
public interface DashboardService {

    /**
     * 获取数据看板概览
     *
     * @param days 趋势数据天数(7或30)
     * @return 数据看板概览
     */
    DashboardOverviewVO getOverview(Integer days);
}
