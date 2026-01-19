package com.dingdong.dto.user;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关系列表展示DTO
 * 包含关系信息和对方用户的详细信息
 * 
 * 重构说明：
 * - 关系现在是平等的，两个用户身份对等
 * - initiatorId: 发起人（邀请者）
 * - partnerId: 伙伴（被邀请者）
 */
@Data
public class RelationDisplayDTO {
    /** 关系ID */
    private Long id;

    /** 发起人ID（邀请者） */
    private Long initiatorId;

    /** 伙伴ID（被邀请者） */
    private Long partnerId;

    /** 关系名称 */
    private String relationName;

    /** 状态: 0-待确认, 1-已接受, 2-已拒绝, 3-解绑中 */
    private Integer status;

    /** 对方用户ID */
    private Long otherUserId;

    /** 对方用户昵称 */
    private String otherNickname;

    /** 对方用户头像 */
    private String otherAvatar;

    /** 当前用户角色: INITIATOR(发起人), PARTNER(伙伴) */
    private String role;

    /** 解绑生效时间（仅当状态为解绑中时有值） */
    private LocalDateTime unbindExpireTime;
}
