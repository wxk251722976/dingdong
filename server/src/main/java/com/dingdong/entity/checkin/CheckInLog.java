package com.dingdong.entity.checkin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dingdong.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 打卡日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_in_log")
public class CheckInLog extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 关联的任务ID */
    private Long taskId;

    /** 打卡时间 */
    private LocalDateTime checkTime;

    /** 状态: 1-正常, 2-补打卡 */
    private Integer status;
}
