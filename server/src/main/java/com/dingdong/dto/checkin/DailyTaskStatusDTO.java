package com.dingdong.dto.checkin;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 每日任务状态DTO
 * 用于返回任务及其完成状态（不包含实体类引用）
 */
@Data
public class DailyTaskStatusDTO {
    /** 任务ID */
    private Long taskId;

    /** 任务标题 */
    private String title;

    /** 提醒时间 */
    private LocalDateTime remindTime;

    /** 重复类型: 0-单次, 1-每天, 2-工作日, 3-周末 */
    private Integer repeatType;

    /** 状态码: 0-待完成, 1-已完成, 2-已错过 */
    private Integer status;

    /** 完成时间（已完成时有值） */
    private LocalDateTime checkTime;
}
