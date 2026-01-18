package com.dingdong.service.notification;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.common.constant.NotifyType;
import com.dingdong.entity.notification.NotificationLog;

import java.time.LocalDate;

/**
 * 通知推送记录服务接口
 */
public interface INotificationLogService extends IService<NotificationLog> {

    /**
     * 检查是否已发送过通知
     *
     * @param taskId     任务ID
     * @param notifyDate 通知日期
     * @param notifyType 通知类型
     * @return 是否已发送
     */
    boolean exists(Long taskId, LocalDate notifyDate, NotifyType notifyType);

    /**
     * 记录通知发送结果
     *
     * @param taskId     任务ID
     * @param notifyDate 通知日期
     * @param notifyType 通知类型
     * @param userId     接收者用户ID
     * @param success    是否成功
     * @param errorMsg   失败原因
     * @return 是否记录成功
     */
    boolean record(Long taskId, LocalDate notifyDate, NotifyType notifyType,
            Long userId, boolean success, String errorMsg);

    /**
     * 尝试获取通知发送锁（Redis + 数据库双保险）
     * 使用 Redis SETNX 实现分布式锁，同时检查数据库记录
     *
     * @param taskId     任务ID
     * @param notifyDate 通知日期
     * @param notifyType 通知类型
     * @return 是否获取成功（true=可以发送，false=已发送过）
     */
    boolean tryAcquireNotifyLock(Long taskId, LocalDate notifyDate, NotifyType notifyType);
}
