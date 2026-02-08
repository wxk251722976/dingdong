package com.dingdong.dto.wechat;

import lombok.Data;

/**
 * 更新动态消息请求DTO
 */
@Data
public class UpdateDynamicMsgDTO {
    /**
     * 动态消息ID
     */
    private String activityId;

    /**
     * 状态 (accepted/rejected)
     */
    private String status;
}
