package com.dingdong.dto.checkin;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务创建/更新请求DTO
 */
@Data
public class TaskDTO {
    /** 任务ID（更新时使用） */
    private Long id;

    /** 创建者ID（监督者） */
    private Long creatorId;

    /** 目标用户ID（被监督者） */
    private Long userId;

    /** 任务标题 */
    private String title;

    /** 提醒时间 */
    private LocalDateTime remindTime;

    /**
     * 重复类型
     * 0-单次, 1-每天, 2-工作日, 3-周末
     */
    private Integer repeatType;
}
