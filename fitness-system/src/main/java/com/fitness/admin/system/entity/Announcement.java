package com.fitness.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
public class Announcement extends BaseEntity {

    private String title;
    private String content;
    private String type;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
}
