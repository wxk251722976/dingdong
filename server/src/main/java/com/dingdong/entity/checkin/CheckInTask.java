package com.dingdong.entity.checkin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dingdong.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 叮咚任务实体类
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

    /** 任务标题/内容 */
    private String title;

    /** 提醒时间 */
    private LocalDateTime remindTime;

    /**
     * 重复类型
     * 0-单次, 1-每天, 2-工作日, 3-周末
     * 
     * @see com.dingdong.common.constant.RepeatType
     */
    private Integer repeatType;

    /** 状态: 1-启用, 0-停用 */
    private Integer status;
}
