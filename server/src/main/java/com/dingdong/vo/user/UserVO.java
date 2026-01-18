package com.dingdong.vo.user;

import lombok.Data;

/**
 * 用户信息 VO
 * 用于前端展示用户信息
 */
@Data
public class UserVO {
    /** 用户ID */
    private Long id;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 用户等级编码 */
    private String levelCode;

    /** 等级名称 */
    private String levelName;
}
