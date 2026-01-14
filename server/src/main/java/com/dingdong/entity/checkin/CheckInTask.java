package com.dingdong.entity.checkin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dingdong.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 打卡任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_in_task")
public class CheckInTask extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者ID (监督者) */
    private Long creatorId;

    /** 目标用户ID (被监督者) */
    private Long userId;

    /** 任务标题/内容 (e.g. "吃药", "喝水") */
    private String title;

    /** 提醒时间 (e.g. 10:00:00) */
    private LocalDateTime remindTime;

    /** 重复规则 (e.g. "1,2,3,4,5" for Weekdays, "1,2,3,4,5,6,7" for Daily) */
    private String repeatDays;

    /** 状态: 1-启用, 0-停用 */
    private Integer status;
}
