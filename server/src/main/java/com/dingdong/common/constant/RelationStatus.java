package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户关系状态枚举
 * 用于表示绑定邀请的确认状态
 */
@Getter
@AllArgsConstructor
public enum RelationStatus {

    /** 待确认 - 邀请已发送，等待对方确认 */
    PENDING(0, "待确认"),

    /** 已接受 - 双方已建立关系 */
    ACCEPTED(1, "已接受"),

    /** 已拒绝 - 对方拒绝了绑定邀请 */
    REJECTED(2, "已拒绝");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举值
     * 
     * @param code 状态码
     * @return 对应的枚举值，未找到返回null
     */
    public static RelationStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RelationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
