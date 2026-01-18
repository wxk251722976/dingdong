package com.dingdong.dto.checkin;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

/**
 * 任务更新DTO
 */
@Data
public class TaskUpdateDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String title;

    @Length(max = 50, message = "任务内容不能超过50个字符")
    private String description;

    private LocalDateTime remindTime;

    private Integer repeatType;
}
