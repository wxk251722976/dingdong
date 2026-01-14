package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRole {
    ELDER("ELDER", "长辈"),
    CHILD("CHILD", "子女");

    private final String code;
    private final String desc;
}
