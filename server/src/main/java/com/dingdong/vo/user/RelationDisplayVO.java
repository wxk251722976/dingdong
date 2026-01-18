package com.dingdong.vo.user;

import lombok.Data;

/**
 * 关系展示 VO
 * 用于展示用户关系列表
 */
@Data
public class RelationDisplayVO {
    /** 关系ID */
    private Long id;

    /** 被监督者ID */
    private Long supervisedId;

    /** 监督者ID */
    private Long supervisorId;

    /** 关系名称 */
    private String relationName;

    /** 状态: 0-待确认, 1-已接受, 2-已拒绝 */
    private Integer status;

    /** 对方用户ID */
    private Long otherUserId;

    /** 对方用户昵称 */
    private String otherNickname;

    /** 对方用户头像 */
    private String otherAvatar;

    /** 当前用户角色: SUPERVISOR(我监督的), SUPERVISED(监督我的) */
    private String role;
}
