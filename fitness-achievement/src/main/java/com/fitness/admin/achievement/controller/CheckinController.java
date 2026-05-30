package com.fitness.admin.achievement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.achievement.entity.Checkin;
import com.fitness.admin.achievement.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "打卡管理")
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckinController extends BaseController {

    private final CheckinService checkinService;

    @Operation(summary = "打卡列表")
    @GetMapping("/list")
    public R<PageResult<Checkin>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Checkin> page = checkinService.queryPage(pageNum, pageSize);
        return page((Page) page);
    }

    @Operation(summary = "打卡统计")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats(@RequestParam(defaultValue = "7") Integer days) {
        return R.ok(checkinService.getStats(days));
    }

    @Operation(summary = "连续打卡排行榜")
    @GetMapping("/leaderboard")
    public R<List<Map<String, Object>>> leaderboard(@RequestParam(defaultValue = "20") Integer limit) {
        return R.ok(checkinService.getLeaderboard(limit));
    }
}
