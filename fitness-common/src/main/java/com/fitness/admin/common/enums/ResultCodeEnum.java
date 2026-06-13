package com.fitness.admin.common.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(4000, "必填参数缺失"),
    PARAM_INVALID(4001, "参数格式不合法"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    PERMISSION_DENIED(4031, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXISTS(1004, "用户已存在"),
    USER_NOT_LOGIN(1005, "用户未登录"),

    TOKEN_EXPIRED(2001, "Token已过期"),
    TOKEN_INVALID(2002, "Token无效"),

    DATA_NOT_FOUND(3001, "数据不存在"),
    DATA_EXISTS(3002, "数据已存在"),
    DATA_OPERATION_FAILED(3003, "数据操作失败"),

    FILE_UPLOAD_ERROR(4002, "文件上传失败"),
    FILE_TOO_LARGE(4003, "文件过大"),
    FILE_TYPE_INVALID(4004, "文件类型不合法"),

    AI_SERVICE_ERROR(5001, "AI服务异常"),
    AI_UPSTREAM_ERROR(5002, "AI上游接口错误"),
    AI_RATE_LIMIT(5003, "AI调用频率超限"),
    VECTOR_DB_ERROR(5004, "向量数据库异常"),
    LLM_TIMEOUT(5005, "AI响应超时"),

    CHECKIN_ALREADY_DONE(6001, "今天已经打过卡了"),
    WORKOUT_ALREADY_RECORDED(6002, "本时段训练已记录"),

    COMMUNITY_SENSITIVE(7001, "内容含敏感词"),
    POST_NOT_FOUND(7002, "帖子不存在"),
    COMMENT_NOT_FOUND(7003, "评论不存在"),

    ANNOUNCEMENT_PUBLISHED(8001, "公告已发布,不可修改"),

    RATE_LIMIT(9001, "请求过于频繁"),
    SYSTEM_ERROR(9999, "系统异常");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
