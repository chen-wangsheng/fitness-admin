package com.fitness.admin.system.aspect;

import com.fitness.admin.common.utils.SecurityUtil;
import com.fitness.admin.common.annotation.LogOperation;
import com.fitness.admin.system.entity.OperationLog;
import com.fitness.admin.system.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(com.fitness.admin.common.annotation.LogOperation)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 先执行业务方法
        Object result = point.proceed();

        // 方法执行成功后异步记录日志（不影响正常业务流程）
        try {
            recordLog(point, result);
        } catch (Exception e) {
            log.warn("操作日志记录失败: {}", e.getMessage());
        }

        return result;
    }

    private void recordLog(ProceedingJoinPoint point, Object result) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        LogOperation annotation = signature.getMethod().getAnnotation(LogOperation.class);
        if (annotation == null) return;

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();

        // 获取当前登录用户 ID
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            log.warn("操作日志记录失败: 无法获取当前用户ID");
            return;
        }

        // 构建日志记录
        OperationLog logEntry = new OperationLog();
        logEntry.setAdminUserId(userId);
        logEntry.setAction(annotation.action());
        logEntry.setModule(annotation.module());
        logEntry.setIpAddress(getClientIp(request));
        logEntry.setUserAgent(request.getHeader("User-Agent"));
        logEntry.setCreatedAt(LocalDateTime.now());

        // 尝试从方法参数中提取 targetId
        String targetId = extractTargetId(point);
        if (targetId != null) {
            logEntry.setTargetId(targetId);
        }

        // 拼接详情：类名.方法名
        String detail = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        logEntry.setDetail(detail);

        operationLogMapper.insert(logEntry);
    }

    /**
     * 从方法参数中提取 targetId（优先取名为 id 的 Long 参数，或 @PathVariable 的参数）
     */
    private String extractTargetId(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();

        if (paramNames == null || args == null) return null;

        for (int i = 0; i < paramNames.length; i++) {
            if ("id".equals(paramNames[i]) && args[i] != null) {
                return args[i].toString();
            }
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
