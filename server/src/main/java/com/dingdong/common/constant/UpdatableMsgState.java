package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动态消息状态枚举
 * target_state: 0-未开始, 1-进行中, 2-已结束
 */
@Getter
@AllArgsConstructor
public enum UpdatableMsgState {

    /** 未开始 */
    UN_START(0, "未开始"),

    /** 进行中 */
    ONGOING(1, "进行中"),

    /** 已结束 */
    ENDED(2, "已结束");

    private final Integer code;
    private final String desc;

    public static UpdatableMsgState fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UpdatableMsgState state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }
}
