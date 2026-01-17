package com.dingdong.common.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统上下文持有者
 * 使用 ThreadLocal 存储当前请求的上下文数据（如用户ID等）
 */
public class SystemContextHolder {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        set(KEY_USER_ID, userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return get(KEY_USER_ID, Long.class);
    }

    /**
     * 设置当前用户名称
     */
    public static void setUsername(String username) {
        set(KEY_USERNAME, username);
    }

    /**
     * 获取当前用户名称
     */
    public static String getUsername() {
        return get(KEY_USERNAME, String.class);
    }

    /**
     * 设置上下文数据
     */
    public static void set(String key, Object value) {
        THREAD_LOCAL.get().put(key, value);
    }

    /**
     * 获取上下文数据
     */
    public static Object get(String key) {
        return THREAD_LOCAL.get().get(key);
    }

    /**
     * 获取上下文数据（带类型转换）
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        Object value = THREAD_LOCAL.get().get(key);
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 清除当前线程的上下文数据
     * 请求结束后务必调用，避免内存泄漏
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }
}
