package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXISTS(1004, "用户已存在"),

    TOKEN_EXPIRED(2001, "Token已过期"),
    TOKEN_INVALID(2002, "Token无效"),

    DATA_NOT_FOUND(3001, "数据不存在"),
    DATA_EXISTS(3002, "数据已存在"),

    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TOO_LARGE(4002, "文件过大"),

    AI_SERVICE_ERROR(5001, "AI服务异常"),
    VECTOR_DB_ERROR(5002, "向量数据库异常"),

    CHECKIN_ALREADY_DONE(6001, "今天已经打过卡了"),

    RATE_LIMIT(9001, "请求过于频繁"),
    SYSTEM_ERROR(9999, "系统异常");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
