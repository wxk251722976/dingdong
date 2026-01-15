package com.dingdong.dto.checkin;

import lombok.Data;
import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

/**
 * 打卡请求参数 DTO
 * 
 * @author Antigravity
 */
@Data
public class CheckInDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 打卡用户ID */
    @NotNull(message = "打卡用户ID不能为空")
    private Long userId;

    /** 关联的任务ID */
    @NotNull(message = "关联的任务ID不能为空")
    private Long taskId;
}
