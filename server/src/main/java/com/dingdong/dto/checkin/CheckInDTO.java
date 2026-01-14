package com.dingdong.dto.checkin;

import lombok.Data;
import java.io.Serializable;

/**
 * 打卡请求参数 DTO
 * 
 * @author Antigravity
 */
@Data
public class CheckInDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 打卡用户ID */
    private Long userId;

    /** 关联的任务ID */
    private Long taskId;
}
