package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum EquipmentEnum {

    NONE("none", "无器械"),
    DUMBBELL("dumbbell", "哑铃"),
    BARBELL("barbell", "杠铃"),
    MACHINE("machine", "器械"),
    CABLE("cable", "绳索"),
    BAND("band", "弹力带");

    private final String code;
    private final String desc;

    EquipmentEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
