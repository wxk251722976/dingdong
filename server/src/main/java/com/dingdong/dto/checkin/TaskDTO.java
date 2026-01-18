package com.dingdong.dto.checkin;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 任务创建/更新请求DTO
 */
@Data
public class TaskDTO {
    /** 任务ID（更新时使用） */
    private Long id;

    /** 创建者ID（监督者） */
    @NotNull(message = "创建者ID不能为空")
    private Long creatorId;

    /** 目标用户ID（被监督者） */
    @NotNull(message = "目标用户ID不能为空")
    private Long userId;

    /** 任务标题 */
    @NotBlank(message = "任务标题不能为空")
    private String title;

    /** 任务详细内容 */
    @org.hibernate.validator.constraints.Length(max = 50, message = "任务内容不能超过50个字符")
    private String description;

    /** 提醒时间 */
    @NotNull(message = "提醒时间不能为空")
    private LocalDateTime remindTime;

    /**
     * 重复类型
     * 0-单次, 1-每天, 2-工作日, 3-周末
     */
    @NotNull(message = "重复类型不能为空")
    private Integer repeatType;
}
