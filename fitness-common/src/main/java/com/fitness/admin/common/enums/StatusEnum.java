package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum StatusEnum {

    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final Integer code;
    private final String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
