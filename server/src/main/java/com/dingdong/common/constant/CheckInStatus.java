package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打卡状态枚举
 */
@Getter
@AllArgsConstructor
public enum CheckInStatus {
    NORMAL(1, "正常打卡"),
    REISSUE(2, "补打卡");

    private final Integer code;
    private final String desc;
}
