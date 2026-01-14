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
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 老人ID */
    private Long elderId;

    /** 子女ID (监督者) */
    private Long childId;

    /** 关系名称 (e.g. 情侣, 挚友) */
    private String relationName;

    /** 状态: 0-待确认, 1-已确认 */
    private Integer status;
}
