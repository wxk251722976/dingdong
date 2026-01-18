package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
@AllArgsConstructor
public enum NotifyType {

    /** 叮咚提醒 - 在任务开始前30分钟通知被监督者 */
    REMIND(1, "叮咚提醒"),

    /** 打卡完成 - 被监督者完成打卡后通知监督者 */
    CHECK_IN_COMPLETE(2, "打卡完成"),

    /** 漏打卡 - 超过任务时间30分钟未打卡通知监督者 */
    MISSED(3, "漏打卡"),

    /** 补打卡 - 补打卡成功后通知监督者 */
    MAKE_UP(4, "补打卡");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取枚举
     */
    public static NotifyType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotifyType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
