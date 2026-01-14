package com.dingdong.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dingdong.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信OpenID */
    private String openid;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 角色: ELDER-老人, CHILD-子女 */
    private String role;
}
