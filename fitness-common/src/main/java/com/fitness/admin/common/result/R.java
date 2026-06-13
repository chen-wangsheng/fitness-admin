package com.fitness.admin.common.result;

import com.fitness.admin.common.enums.ResultCodeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isSuccess() {
        return ResultCodeEnum.SUCCESS.getCode().equals(this.code);
    }

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(ResultCodeEnum.SUCCESS.getCode());
        r.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = ok();
        r.setData(data);
        return r;
    }

    /**
     * 业务错误(枚举驱动)。推荐用法:return R.error(ResultCodeEnum.USER_NOT_FOUND);
     */
    public static <T> R<T> error(ResultCodeEnum resultCodeEnum) {
        R<T> r = new R<>();
        r.setCode(resultCodeEnum.getCode());
        r.setMessage(resultCodeEnum.getMessage());
        return r;
    }

    /**
     * 业务错误(显式 code + message)。用于需要自定义 code 但复用 ResultCodeEnum 中尚未定义的场景。
     */
    public static <T> R<T> error(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
