package com.fitness.admin.user.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String keyword;
    private Integer gender;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer status;
    private Long tagId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
