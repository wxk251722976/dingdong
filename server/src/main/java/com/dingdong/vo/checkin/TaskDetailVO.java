package com.dingdong.vo.checkin;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务详情 VO
 */
@Data
public class TaskDetailVO {
    /** 任务ID */
    private Long taskId;

    /** 任务标题 */
    private String title;

    /** 任务详细内容 */
    private String description;

    /** 提醒时间 */
    private LocalDateTime remindTime;

    /** 重复类型: 0-单次, 1-每天, 2-工作日, 3-周末 */
    private Integer repeatType;

    /** 目标用户ID（被监督者） */
    private Long userId;

    /** 目标用户昵称 */
    private String userName;

    /** 任务创建时间 */
    private LocalDateTime createTime;

    // 创建者/监督者信息

    /** 创建者ID（监督者） */
    private Long creatorId;

    /** 创建者昵称 */
    private String creatorName;

    /** 创建者头像 */
    private String creatorAvatar;
}
