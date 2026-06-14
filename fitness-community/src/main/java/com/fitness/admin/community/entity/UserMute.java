package com.fitness.admin.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_mute")
public class UserMute extends BaseEntity {

    private Long userId;
    private String reason;
    private Integer durationDays;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Long operatorId;
    private Integer status;
}
