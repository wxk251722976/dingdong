package com.dingdong.vo.user;

import lombok.Data;

/**
 * 基础用户关系 VO
 * 对应 UserRelation 实体
 */
@Data
public class UserRelationVO {
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
}
