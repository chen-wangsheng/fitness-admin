package com.fitness.admin.common.result;

import com.fitness.admin.common.enums.ResultCodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RTest {

    @Test
    void ok_shouldReturnSuccessCode() {
        R<String> r = R.ok();
        assertEquals(ResultCodeEnum.SUCCESS.getCode(), r.getCode());
        assertEquals("操作成功", r.getMessage());
        assertNotNull(r.getTimestamp());
        assertNull(r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void okWithData_shouldCarryData() {
        R<String> r = R.ok("hello");
        assertEquals("hello", r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void errorByEnum_shouldUseEnumCodeAndMessage() {
        R<Void> r = R.error(ResultCodeEnum.USER_NOT_FOUND);
        assertEquals(ResultCodeEnum.USER_NOT_FOUND.getCode(), r.getCode());
        assertEquals(ResultCodeEnum.USER_NOT_FOUND.getMessage(), r.getMessage());
        assertFalse(r.isSuccess());
    }

    @Test
    void errorByCodeAndMessage_shouldPreserveBoth() {
        R<Void> r = R.error(4031, "无访问权限");
        assertEquals(4031, r.getCode());
        assertEquals("无访问权限", r.getMessage());
    }
}
