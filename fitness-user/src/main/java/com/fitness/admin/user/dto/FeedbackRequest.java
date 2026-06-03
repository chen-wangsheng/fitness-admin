package com.fitness.admin.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 5, max = 500, message = "反馈内容长度5-500字")
    private String content;
}
