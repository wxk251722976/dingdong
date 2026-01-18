package com.dingdong.entity.notification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 通知推送记录实体类
 */
@Data
@TableName("notification_log")
public class NotificationLog {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 任务ID */
    private Long taskId;

    /** 通知日期 */
    private LocalDate notifyDate;

    /**
     * 通知类型
     * 1-叮咚提醒, 2-打卡完成, 3-漏打卡, 4-补打卡
     * 
     * @see com.dingdong.common.constant.NotifyType
     */
    private Integer notifyType;

    /** 接收者用户ID */
    private Long userId;

    /** 状态: 1-成功, 0-失败 */
    private Integer status;

    /** 失败原因 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;
}
