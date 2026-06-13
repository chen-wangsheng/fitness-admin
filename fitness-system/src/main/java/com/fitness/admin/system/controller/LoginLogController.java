package com.fitness.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.system.entity.LoginLog;
import com.fitness.admin.system.service.LoginLogService;
import com.fitness.admin.system.vo.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "登录日志")
@RestController
@RequestMapping("/login-log")
@RequiredArgsConstructor
@SaCheckPermission("log:login:read")
public class LoginLogController extends BaseController {

    private final LoginLogService loginLogService;

    @Operation(summary = "登录日志列表")
    @GetMapping("/list")
    public R<PageResult<LoginLogVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<LoginLog> page = loginLogService.queryPage(pageNum, pageSize);

        List<LoginLogVO> voList = page.getRecords().stream().map(log -> {
            LoginLogVO vo = new LoginLogVO();
            vo.setId(log.getId());
            vo.setUsername(log.getUsername());
            vo.setLoginTime(log.getCreatedAt());
            vo.setIp(log.getIpAddress());
            vo.setUserAgent(log.getUserAgent());
            vo.setStatus(log.getLoginStatus() == 1 ? "success" : "fail");
            return vo;
        }).collect(Collectors.toList());

        PageResult<LoginLogVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());

        return R.ok(result);
    }
}
