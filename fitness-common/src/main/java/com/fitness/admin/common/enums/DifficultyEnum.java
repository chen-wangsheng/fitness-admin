package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum DifficultyEnum {

    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final Integer code;
    private final String desc;

    DifficultyEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
