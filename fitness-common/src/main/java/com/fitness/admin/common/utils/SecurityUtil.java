package com.fitness.admin.common.utils;

import cn.dev33.satoken.stp.StpUtil;

public class SecurityUtil {

    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    public static void checkLogin() {
        StpUtil.checkLogin();
    }
}
