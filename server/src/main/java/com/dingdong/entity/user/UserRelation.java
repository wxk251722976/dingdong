package com.dingdong.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dingdong.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户关系实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_relation")
public class UserRelation extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 被监督者ID */
    private Long supervisedId;

    /** 监督者ID */
    private Long supervisorId;

    /** 关系名称 (e.g. 情侣, 挚友) */
    private String relationName;

    /** 状态: 0-待确认, 1-已接受, 2-已拒绝 */
    private Integer status;
}
