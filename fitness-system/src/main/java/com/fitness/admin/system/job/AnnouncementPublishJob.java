package com.fitness.admin.system.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.admin.system.entity.Announcement;
import com.fitness.admin.system.mapper.AnnouncementMapper;
import com.fitness.admin.common.service.EmailService;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告定时发布任务
 * 每分钟扫描一次，将到期的定时公告自动发布
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementPublishJob {

    private final AnnouncementMapper announcementMapper;
    private final UserMapper userMapper;
    private final EmailService emailService;

    /**
     * 每小时执行一次，检查是否有到期的定时公告需要发布
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void publishScheduledAnnouncements() {
        LocalDateTime now = LocalDateTime.now();

        // 查询所有待发布且发布时间已到的公告
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 0) // 草稿状态
               .isNotNull(Announcement::getPublishTime)
               .le(Announcement::getPublishTime, now);

        List<Announcement> announcements = announcementMapper.selectList(wrapper);

        if (announcements.isEmpty()) {
            return;
        }

        log.info("发现{}条待定时发布的公告", announcements.size());

        for (Announcement announcement : announcements) {
            try {
                // 更新状态为已发布
                announcement.setStatus(1);
                announcement.setUpdatedAt(now);
                announcementMapper.updateById(announcement);
                log.info("公告定时发布成功: id={}, title={}", announcement.getId(), announcement.getTitle());

                // 发送邮件通知
                sendEmailNotification(announcement);
            } catch (Exception e) {
                log.error("公告定时发布失败: id={}, error={}", announcement.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 向所有有邮箱的用户发送公告邮件通知
     */
    private void sendEmailNotification(Announcement announcement) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getStatus, 1)
                   .isNotNull(User::getEmail)
                   .ne(User::getEmail, "");
        List<User> users = userMapper.selectList(userWrapper);

        if (users.isEmpty()) {
            log.info("没有可通知的用户邮箱，跳过邮件发送");
            return;
        }

        log.info("开始发送公告邮件通知，共{}个用户", users.size());
        for (User user : users) {
            try {
                emailService.sendAnnouncementEmail(user.getEmail(), announcement.getTitle(), announcement.getContent());
            } catch (Exception e) {
                log.error("发送公告邮件失败: userId={}, email={}, error={}", user.getId(), user.getEmail(), e.getMessage());
            }
        }
    }
}
