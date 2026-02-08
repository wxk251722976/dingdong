package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动态消息成员数量枚举
 */
@Getter
@AllArgsConstructor
public enum UpdatableMsgMemberCount {

    /** 1人 (单人/仅自己/被拒绝) */
    ONE("1", "1人"),

    /** 2人 (双人/满员/已接受) */
    TWO("2", "2人");

    private final String value;
    private final String desc;
}
