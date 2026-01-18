package com.dingdong.vo.checkin;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 打卡记录 VO
 * 仅返回前端展示所需字段，避免暴露数据库实体细节
 */
@Data
public class CheckInLogVO {
    /** 记录ID */
    private Long id;

    /** 任务ID */
    private Long taskId;

    /** 打卡时间 */
    private LocalDateTime checkTime;

    /** 状态: 1-正常, 2-补打卡 */
    private Integer status;
}
