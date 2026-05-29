package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum VectorStatusEnum {

    PENDING("pending", "待处理"),
    INDEXING("indexing", "索引中"),
    INDEXED("indexed", "已索引"),
    FAILED("failed", "索引失败");

    private final String code;
    private final String desc;

    VectorStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
