package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 关系历史操作类型枚举
 */
@Getter
@AllArgsConstructor
public enum RelationActionType {

    /** 绑定 - 建立关系 */
    BIND(1, "绑定"),

    /** 发起解绑 - 开始24小时倒计时 */
    UNBIND_INITIATED(2, "发起解绑"),

    /** 撤回解绑 - 取消解绑请求 */
    UNBIND_WITHDRAWN(3, "撤回解绑"),

    /** 解绑完成 - 关系已解除 */
    UNBIND_COMPLETED(4, "解绑完成");

    private final Integer code;
    private final String desc;

    public static RelationActionType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RelationActionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
