package com.fitness.admin.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要记录操作日志的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOperation {

    /** 操作动作，如：新增、编辑、删除 */
    String action();

    /** 所属模块，如：用户管理、内容管理 */
    String module();
}
