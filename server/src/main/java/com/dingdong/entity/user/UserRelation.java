package com.dingdong.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dingdong.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户关系实体类
 * 表示两个用户之间的互助/监督关系（平等关系）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_relation")
public class UserRelation extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 发起人ID（邀请者） */
    private Long initiatorId;

    /** 伙伴ID（被邀请者） */
    private Long partnerId;

    /** 关系名称 (e.g. 情侣, 挚友) */
    private String relationName;

    /** 关系类型: 0-预设, 1-自定义 */
    private Integer relationType;

    /** 状态: 0-待确认, 1-已接受, 2-已拒绝, 3-解绑中 */
    private Integer status;

    /** 解绑生效时间（发起解绑后24小时） */
    private LocalDateTime unbindExpireTime;
}
