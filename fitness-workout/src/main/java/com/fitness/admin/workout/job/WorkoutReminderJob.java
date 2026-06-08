package com.fitness.admin.workout.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.content.entity.PlanDay;
import com.fitness.admin.content.entity.WorkoutPlan;
import com.fitness.admin.content.mapper.PlanDayMapper;
import com.fitness.admin.content.mapper.PlanMapper;
import com.fitness.admin.common.service.EmailService;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import com.fitness.admin.workout.service.WxSubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 训练提醒定时任务
 * 每天早上8点检查当天有训练计划的用户，推送训练提醒
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkoutReminderJob {

    private final UserMapper userMapper;
    private final PlanMapper planMapper;
    private final PlanDayMapper planDayMapper;
    private final WxSubscribeMessageService wxSubscribeMessageService;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每天早上8点执行，推送训练提醒
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendWorkoutReminders() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        // 数据库中dayOfWeek: 1=周一...7=周日
        int dayValue = dayOfWeek.getValue();

        log.info("开始执行训练提醒推送: date={}, dayOfWeek={}", today, dayValue);

        // 查询所有活跃用户（有当前训练计划的）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getStatus, 1)
                   .isNotNull(User::getCurrentPlanId);
        List<User> users = userMapper.selectList(userWrapper);

        if (users.isEmpty()) {
            log.info("没有需要提醒的用户");
            return;
        }

        log.info("共{}个用户需要检查训练提醒", users.size());

        for (User user : users) {
            try {
                processUserReminder(user, today, dayValue);
            } catch (Exception e) {
                log.error("处理用户训练提醒失败: userId={}, error={}", user.getId(), e.getMessage(), e);
            }
        }

        log.info("训练提醒推送完成");
    }

    /**
     * 处理单个用户的训练提醒
     */
    private void processUserReminder(User user, LocalDate today, int dayValue) {
        Long planId = user.getCurrentPlanId();

        // 查询训练计划
        WorkoutPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            return;
        }

        // 查询当天是否有训练安排
        LambdaQueryWrapper<PlanDay> dayWrapper = new LambdaQueryWrapper<>();
        dayWrapper.eq(PlanDay::getPlanId, planId)
                  .eq(PlanDay::getDayOfWeek, dayValue);
        PlanDay planDay = planDayMapper.selectOne(dayWrapper);

        if (planDay == null || Integer.valueOf(1).equals(planDay.getIsRestDay())) {
            // 当天没有训练安排或休息日
            return;
        }

        String planName = plan.getName();
        String dateStr = today.format(DATE_FORMATTER);
        String remark = "今天有训练安排，别忘了完成哦！";

        // 1. 推送小程序订阅消息
        if (user.getOpenid() != null) {
            wxSubscribeMessageService.sendWorkoutReminder(user.getOpenid(), planName, dateStr, remark);
        }

        // 2. 发送邮件提醒
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendWorkoutReminderEmail(user.getEmail(), user.getNickname(), planName);
        }

        log.info("用户训练提醒推送完成: userId={}, planName={}", user.getId(), planName);
    }
}
