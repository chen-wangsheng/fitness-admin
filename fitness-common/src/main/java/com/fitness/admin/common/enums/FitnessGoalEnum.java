package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum FitnessGoalEnum {

    LOSE_FAT("lose_fat", "减脂"),
    GAIN_MUSCLE("gain_muscle", "增肌"),
    KEEP_FIT("keep_fit", "保持身材"),
    IMPROVE_ENDURANCE("improve_endurance", "提升耐力");

    private final String code;
    private final String desc;

    FitnessGoalEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
