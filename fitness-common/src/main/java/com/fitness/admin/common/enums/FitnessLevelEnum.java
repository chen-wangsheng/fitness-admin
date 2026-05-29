package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum FitnessLevelEnum {

    BEGINNER("beginner", "初级"),
    INTERMEDIATE("intermediate", "中级"),
    ADVANCED("advanced", "高级");

    private final String code;
    private final String desc;

    FitnessLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
