package com.fitness.admin.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fitness.admin.community.entity.UserMute;
import com.fitness.admin.community.mapper.UserMuteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserMuteService {

    private final UserMuteMapper userMuteMapper;

    /**
     * 给用户创建禁言记录。durationDays = -1 表示永久。
     * 同一用户已有生效中的禁言时,直接抛错避免叠加。
     */
    @Transactional
    public void mute(Long userId, Integer durationDays, String reason, Long operatorId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (durationDays == null) {
            throw new IllegalArgumentException("durationDays 不能为空");
        }
        if (durationDays < -1 || durationDays == 0) {
            throw new IllegalArgumentException("durationDays 仅支持 -1=永久 或正整数天数");
        }

        // 检查已有生效中禁言
        UserMute active = findActive(userId);
        if (active != null) {
            throw new IllegalStateException("该用户已有生效中的禁言记录");
        }

        UserMute mute = new UserMute();
        mute.setUserId(userId);
        mute.setReason(reason == null ? "" : reason);
        mute.setDurationDays(durationDays);
        mute.setStartAt(LocalDateTime.now());
        mute.setEndAt(durationDays == -1 ? null : LocalDateTime.now().plusDays(durationDays));
        mute.setOperatorId(operatorId);
        mute.setStatus(1);
        userMuteMapper.insert(mute);
    }

    /**
     * 查询用户当前是否被禁言(状态=1 且 未到 end_at)。
     * 顺带把过期的标记为已过期(status=2)。
     */
    public UserMute findActive(Long userId) {
        // 先把过期的全部标记为 status=2
        LambdaUpdateWrapper<UserMute> expire = new LambdaUpdateWrapper<>();
        expire.eq(UserMute::getStatus, 1)
              .isNotNull(UserMute::getEndAt)
              .lt(UserMute::getEndAt, LocalDateTime.now())
              .set(UserMute::getStatus, 2);
        userMuteMapper.update(null, expire);

        LambdaQueryWrapper<UserMute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMute::getUserId, userId)
               .eq(UserMute::getStatus, 1)
               .and(w -> w.isNull(UserMute::getEndAt).or().gt(UserMute::getEndAt, LocalDateTime.now()))
               .orderByDesc(UserMute::getStartAt)
               .last("LIMIT 1");
        return userMuteMapper.selectOne(wrapper);
    }
}
