package com.fitness.admin.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResultCodeEnumTest {

    @Test
    void successCodeIs200() {
        assertEquals(200, ResultCodeEnum.SUCCESS.getCode());
        assertEquals("操作成功", ResultCodeEnum.SUCCESS.getMessage());
    }

    @Test
    void allCodesAreUnique() {
        ResultCodeEnum[] values = ResultCodeEnum.values();
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                if (values[i].getCode().equals(values[j].getCode())) {
                    throw new AssertionError(String.format("重复枚举 code: %s = %s", values[i], values[j]));
                }
            }
        }
    }

    @Test
    void businessCodesArePopulated() {
        assertNotNull(ResultCodeEnum.USER_NOT_FOUND);
        assertNotNull(ResultCodeEnum.AI_UPSTREAM_ERROR);
        assertNotNull(ResultCodeEnum.RATE_LIMIT);
        assertNotNull(ResultCodeEnum.PERMISSION_DENIED);
        assertNotNull(ResultCodeEnum.COMMUNITY_SENSITIVE);
    }
}
