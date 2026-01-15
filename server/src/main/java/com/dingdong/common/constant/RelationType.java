package com.dingdong.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 关系类型枚举
 * 对应界面上的关系选项
 */
@Getter
@AllArgsConstructor
public enum RelationType {

    /** 自定义关系 - 用户自行填写的关系名称 */
    CUSTOM(0, "自定义"),

    /** 情侣关系 */
    COUPLE(1, "情侣"),

    /** 挚友关系 */
    FRIEND(2, "挚友"),

    /** 家人关系 */
    FAMILY(3, "家人");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举值
     * 
     * @param code 类型码
     * @return 对应的枚举值，未找到返回CUSTOM
     */
    public static RelationType fromCode(Integer code) {
        if (code == null) {
            return CUSTOM;
        }
        for (RelationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
