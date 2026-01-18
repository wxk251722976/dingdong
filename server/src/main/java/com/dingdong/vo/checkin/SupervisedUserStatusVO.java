package com.dingdong.vo.checkin;

import lombok.Data;

/**
 * 被监督用户状态 VO
 * 用于展示被监督者的今日完成情况
 */
@Data
public class SupervisedUserStatusVO {
    /** 用户ID */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 关系名称 */
    private String relationName;

    /** 今日整体状态: 0-进行中, 1-已完成, 3-已错过 */
    private Integer todayStatus;
}
