package com.fitness.admin.user.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailVO extends UserVO {

    private String aiProfile;
    private Integer achievementCount;
    private Integer checkinCount;
}
