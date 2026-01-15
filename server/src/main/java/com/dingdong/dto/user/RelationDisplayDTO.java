package com.dingdong.dto.user;

import lombok.Data;

/**
 * 关系列表展示DTO
 * 包含关系信息和对方用户的详细信息
 */
@Data
public class RelationDisplayDTO {
    /** 关系ID */
    private Long id;

    /** 被监督者ID */
    private Long supervisedId;

    /** 监督者ID */
    private Long supervisorId;

    /** 关系名称 */
    private String relationName;

    /** 状态: 0-待确认, 1-已确认 */
    private Integer status;

    /** 对方用户ID（根据查询角度不同，可能是监督者或被监督者） */
    private Long otherUserId;

    /** 对方用户昵称 */
    private String otherNickname;

    /** 对方用户头像 */
    private String otherAvatar;

    /** 当前用户角色: SUPERVISOR(我监督的), SUPERVISED(监督我的) */
    private String role;
}
