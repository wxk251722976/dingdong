package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务重复类型枚举
 */
@Getter
@AllArgsConstructor
public enum RepeatType {

    /** 单次任务 - 只在指定日期执行一次 */
    ONCE(0, "单次"),

    /** 每天 - 每天都执行 */
    DAILY(1, "每天"),

    /** 工作日 - 周一至周五 */
    WEEKDAYS(2, "工作日"),

    /** 周末 - 周六和周日 */
    WEEKENDS(3, "周末");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举值
     */
    public static RepeatType fromCode(Integer code) {
        if (code == null) {
            return ONCE;
        }
        for (RepeatType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return ONCE;
    }

    /**
     * 判断指定星期几是否在重复规则内
     * 
     * @param dayOfWeek 星期几 (1=周一, 7=周日)
     * @return 是否匹配
     */
    public boolean matchesDayOfWeek(int dayOfWeek) {
        switch (this) {
            case DAILY:
                return true;
            case WEEKDAYS:
                return dayOfWeek >= 1 && dayOfWeek <= 5;
            case WEEKENDS:
                return dayOfWeek == 6 || dayOfWeek == 7;
            case ONCE:
            default:
                return true; // 单次任务由日期匹配决定
        }
    }
}
