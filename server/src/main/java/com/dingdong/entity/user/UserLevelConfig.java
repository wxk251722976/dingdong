package com.dingdong.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dingdong.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户等级配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_level_config")
public class UserLevelConfig extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 等级编码 */
    private String levelCode;

    /** 等级名称 */
    private String levelName;

    /** 最大监督人数 */
    private Integer maxSupervisedCount;

    /** 每人最大任务数 */
    private Integer maxTaskPerUser;

    /** 每日最大反馈数 */
    private Integer maxFeedbackPerDay;

    /** 等级描述 */
    private String description;

    /** 排序 */
    private Integer sortOrder;
}
