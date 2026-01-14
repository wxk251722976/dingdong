package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务完成状态枚举
 * 用于表示每日任务的执行状态
 */
@Getter
@AllArgsConstructor
public enum TaskStatus {

    /** 待完成 - 任务尚未到提醒时间或正在等待执行 */
    PENDING(0, "待完成"),

    /** 已完成 - 任务已被用户完成打卡 */
    COMPLETED(1, "已完成"),

    /** 已错过 - 任务提醒时间已过但未完成 */
    MISSED(2, "已错过");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举值
     * 
     * @param code 状态码
     * @return 对应的枚举值，未找到返回null
     */
    public static TaskStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
