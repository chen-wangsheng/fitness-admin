package com.fitness.admin.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件发送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * 异步发送简单文本邮件
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("简单邮件发送成功: to={}", to);
        } catch (Exception e) {
            log.error("简单邮件发送失败: to={}, error={}", to, e.getMessage(), e);
        }
    }

    /**
     * 异步发送HTML格式邮件
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("HTML邮件发送成功: to={}", to);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败: to={}, error={}", to, e.getMessage(), e);
        }
    }

    /**
     * 发送公告通知邮件
     */
    @Async
    public void sendAnnouncementEmail(String to, String title, String content) {
        String htmlContent = buildAnnouncementHtml(title, content);
        sendHtmlEmail(to, "【健身助手公告】" + title, htmlContent);
    }

    /**
     * 发送训练提醒邮件
     */
    @Async
    public void sendWorkoutReminderEmail(String to, String nickname, String planName) {
        String htmlContent = buildWorkoutReminderHtml(nickname, planName);
        sendHtmlEmail(to, "【健身助手】训练提醒", htmlContent);
    }

    private String buildAnnouncementHtml(String title, String content) {
        return """
            <div style="max-width:600px;margin:0 auto;font-family:Arial,sans-serif;">
                <div style="background:#409EFF;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0;">
                    <h2 style="margin:0;">健身助手公告</h2>
                </div>
                <div style="padding:20px;border:1px solid #eee;border-top:none;border-radius:0 0 8px 8px;">
                    <h3 style="color:#333;">%s</h3>
                    <div style="color:#666;line-height:1.8;">%s</div>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0;">
                    <p style="color:#999;font-size:12px;">此邮件由系统自动发送，请勿回复</p>
                </div>
            </div>
            """.formatted(title, content);
    }

    private String buildWorkoutReminderHtml(String nickname, String planName) {
        return """
            <div style="max-width:600px;margin:0 auto;font-family:Arial,sans-serif;">
                <div style="background:#67C23A;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0;">
                    <h2 style="margin:0;">训练提醒</h2>
                </div>
                <div style="padding:20px;border:1px solid #eee;border-top:none;border-radius:0 0 8px 8px;">
                    <p style="color:#333;">Hi %s，</p>
                    <p style="color:#666;line-height:1.8;">你今天的训练计划是：<strong>%s</strong></p>
                    <p style="color:#666;line-height:1.8;">坚持锻炼，保持好身材！加油！</p>
                    <hr style="border:none;border-top:1px solid #eee;margin:20px 0;">
                    <p style="color:#999;font-size:12px;">此邮件由系统自动发送，请勿回复</p>
                </div>
            </div>
            """.formatted(nickname, planName);
    }
}
