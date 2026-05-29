package com.fitness.admin.achievement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin")
public class Checkin extends BaseEntity {

    private Long userId;
    private LocalDate checkinDate;
    private Integer continuousDays;
    private Integer totalDays;
    private String notes;
}
