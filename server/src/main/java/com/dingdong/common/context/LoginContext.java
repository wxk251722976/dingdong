package com.dingdong.common.context;

import com.dingdong.entity.user.SysUser;

/**
 * 登录用户上下文
 * 使用 ThreadLocal 存储当前登录用户信息
 */
public class LoginContext {

    private static final ThreadLocal<SysUser> CURRENT_USER = new ThreadLocal<>();

    /**
     * 设置当前登录用户
     */
    public static void setCurrentUser(SysUser user) {
        CURRENT_USER.set(user);
    }

    /**
     * 获取当前登录用户
     */
    public static SysUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        SysUser user = CURRENT_USER.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名称
     */
    public static String getCurrentUserName() {
        SysUser user = CURRENT_USER.get();
        return user != null ? user.getNickname() : null;
    }

    /**
     * 清除当前登录用户（请求结束时调用）
     */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
