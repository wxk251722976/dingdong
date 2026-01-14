package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务启用状态枚举
 * 用于控制任务是否生效
 */
@Getter
@AllArgsConstructor
public enum TaskEnabled {

    /** 停用 - 任务暂停，不会出现在每日任务列表 */
    DISABLED(0, "停用"),

    /** 启用 - 任务正常生效 */
    ENABLED(1, "启用");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举值
     * 
     * @param code 状态码
     * @return 对应的枚举值，未找到返回null
     */
    public static TaskEnabled fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskEnabled status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
