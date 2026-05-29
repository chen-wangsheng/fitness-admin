package com.fitness.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseData;
    private Integer status;
    private String errorMsg;
    private Long duration;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}
