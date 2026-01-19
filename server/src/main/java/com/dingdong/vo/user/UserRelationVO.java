package com.dingdong.vo.user;

import lombok.Data;

/**
 * 基础用户关系 VO
 * 对应 UserRelation 实体（平等关系）
 */
@Data
public class UserRelationVO {
    /** 关系ID */
    private Long id;

    /** 发起人ID（邀请者） */
    private Long initiatorId;

    /** 伙伴ID（被邀请者） */
    private Long partnerId;

    /** 关系名称 */
    private String relationName;

    /** 状态: 0-待确认, 1-已接受, 2-已拒绝, 3-解绑中, 4-已解绑 */
    private Integer status;
}
