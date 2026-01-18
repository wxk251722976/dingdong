package com.dingdong.vo.checkin;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 每日任务状态 VO
 * 用于前端展示任务列表及状态
 */
@Data
public class DailyTaskStatusVO {
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
