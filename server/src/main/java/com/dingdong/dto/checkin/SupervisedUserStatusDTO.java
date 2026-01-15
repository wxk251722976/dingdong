package com.dingdong.dto.checkin;

import lombok.Data;

/**
 * 被监督用户状态 DTO
 */
@Data
public class SupervisedUserStatusDTO {
    /** 用户ID */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 关系名称 */
    private String relationName;

    /** 今日整体状态: 0-进行中(Pending), 1-已完成(Completed), 3-已错过(Missed) */
    private Integer todayStatus;
}
