package com.fitness.admin.user.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailVO extends UserVO {

    private String openid;
    private BigDecimal bmi;
    private Integer achievementCount;
    private Integer checkinCount;
}
