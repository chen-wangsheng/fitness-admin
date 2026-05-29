package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum ExerciseTypeEnum {

    STRENGTH("strength", "力量训练"),
    CARDIO("cardio", "有氧运动"),
    FLEXIBILITY("flexibility", "柔韧性训练"),
    BALANCE("balance", "平衡训练");

    private final String code;
    private final String desc;

    ExerciseTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
