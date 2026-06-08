package com.fitness.admin.system.listener;

import com.fitness.admin.common.event.LoginEvent;
import com.fitness.admin.system.entity.LoginLog;
import com.fitness.admin.system.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener {

    private final LoginLogMapper loginLogMapper;

    @Async
    @EventListener
    public void handleLoginEvent(LoginEvent event) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setAdminUserId(event.getAdminUserId());
            loginLog.setUsername(event.getUsername());
            loginLog.setIpAddress(event.getIpAddress());
            loginLog.setUserAgent(event.getUserAgent());
            loginLog.setLoginStatus(event.getLoginStatus());
            loginLog.setFailReason(event.getFailReason());
            loginLog.setCreatedAt(java.time.LocalDateTime.now());
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage(), e);
        }
    }
}
