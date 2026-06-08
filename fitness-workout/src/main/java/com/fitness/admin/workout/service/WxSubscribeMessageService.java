package com.fitness.admin.workout.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.fitness.admin.user.entity.User;
import com.fitness.admin.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小程序订阅消息推送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxSubscribeMessageService {

    private final WxMaService wxMaService;
    private final UserMapper userMapper;

    /**
     * 训练提醒模板ID（需在微信后台配置后替换）
     * 模板内容示例：
     * 训练提醒
     * 计划名称：{{thing1}}
     * 训练日：{{date2}}
     * 提醒内容：{{thing3}}
     */
    private static final String WORKOUT_REMINDER_TEMPLATE_ID = "YOUR_TEMPLATE_ID";

    /**
     * 异步发送训练提醒订阅消息
     *
     * @param openid   用户openid
     * @param planName 计划名称
     * @param date     训练日期
     * @param remark   提醒内容
     */
    @Async
    public void sendWorkoutReminder(String openid, String planName, String date, String remark) {
        try {
            WxMaSubscribeMessage message = new WxMaSubscribeMessage();
            message.setToUser(openid);
            message.setTemplateId(WORKOUT_REMINDER_TEMPLATE_ID);
            message.addData(new WxMaSubscribeMessage.MsgData("thing1", planName));
            message.addData(new WxMaSubscribeMessage.MsgData("date2", date));
            message.addData(new WxMaSubscribeMessage.MsgData("thing3", remark));

            wxMaService.getMsgService().sendSubscribeMsg(message);
            log.info("训练提醒推送成功: openid={}", openid);
        } catch (WxErrorException e) {
            log.error("训练提醒推送失败: openid={}, error={}", openid, e.getError().getErrorMsg());
        }
    }

    /**
     * 批量发送训练提醒订阅消息
     *
     * @param users   用户列表
     * @param planName 计划名称
     * @param date     训练日期
     * @param remark   提醒内容
     */
    public void batchSendWorkoutReminder(List<User> users, String planName, String date, String remark) {
        int successCount = 0;
        int failCount = 0;
        for (User user : users) {
            try {
                sendWorkoutReminder(user.getOpenid(), planName, date, remark);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("训练提醒推送失败: userId={}, error={}", user.getId(), e.getMessage());
            }
        }
        log.info("训练提醒推送完成: 成功={}, 失败={}", successCount, failCount);
    }
}
