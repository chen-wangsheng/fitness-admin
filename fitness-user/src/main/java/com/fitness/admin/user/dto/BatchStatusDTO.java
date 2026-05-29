package com.fitness.admin.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchStatusDTO {

    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    private Integer status;
    private String reason;
}
