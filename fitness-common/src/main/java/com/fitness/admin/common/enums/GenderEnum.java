package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {

    MALE(1, "男"),
    FEMALE(2, "女"),
    UNKNOWN(0, "未知");

    private final Integer code;
    private final String desc;

    GenderEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
