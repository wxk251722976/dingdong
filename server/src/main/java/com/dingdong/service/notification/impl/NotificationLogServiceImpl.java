package com.dingdong.service.notification.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.NotifyType;
import com.dingdong.common.util.RedisUtil;
import com.dingdong.entity.notification.NotificationLog;
import com.dingdong.mapper.notification.NotificationLogMapper;
import com.dingdong.service.notification.INotificationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 通知推送记录服务实现类
 * 使用 Redis + 数据库双保险机制防止重复推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationLogServiceImpl extends ServiceImpl<NotificationLogMapper, NotificationLog>
        implements INotificationLogService {

    private final RedisUtil redisUtil;

    /** Redis Key 前缀 */
    private static final String NOTIFY_KEY_PREFIX = "notify:";

    /** Redis Key 过期时间（25小时，确保跨天后清理） */
    private static final long NOTIFY_KEY_EXPIRE_HOURS = 25;

    @Override
    public boolean exists(Long taskId, LocalDate notifyDate, NotifyType notifyType) {
        return count(new LambdaQueryWrapper<NotificationLog>()
                .eq(NotificationLog::getTaskId, taskId)
                .eq(NotificationLog::getNotifyDate, notifyDate)
                .eq(NotificationLog::getNotifyType, notifyType.getCode())) > 0;
    }

    @Override
    public boolean record(Long taskId, LocalDate notifyDate, NotifyType notifyType,
            Long userId, boolean success, String errorMsg) {
        try {
            NotificationLog log = new NotificationLog();
            log.setTaskId(taskId);
            log.setNotifyDate(notifyDate);
            log.setNotifyType(notifyType.getCode());
            log.setUserId(userId);
            log.setStatus(success ? 1 : 0);
            log.setErrorMsg(errorMsg);
            log.setCreateTime(LocalDateTime.now());
            return save(log);
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突，说明已经记录过（并发场景）
            log.debug("通知记录已存在: taskId={}, date={}, type={}", taskId, notifyDate, notifyType);
            return false;
        }
    }

    @Override
    public boolean tryAcquireNotifyLock(Long taskId, LocalDate notifyDate, NotifyType notifyType) {
        String redisKey = buildRedisKey(taskId, notifyDate, notifyType);

        // 1. Redis 快速判断（高性能）
        if (Boolean.TRUE.equals(redisUtil.hasKey(redisKey))) {
            log.debug("Redis 中已存在通知记录: {}", redisKey);
            return false;
        }

        // 2. 数据库二次确认（兜底）
        if (exists(taskId, notifyDate, notifyType)) {
            // 数据库有记录但 Redis 没有，补充 Redis
            redisUtil.set(redisKey, "1", NOTIFY_KEY_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("数据库中已存在通知记录: taskId={}, date={}, type={}", taskId, notifyDate, notifyType);
            return false;
        }

        // 3. 设置 Redis 标记（先占位，防止并发）
        redisUtil.set(redisKey, "1", NOTIFY_KEY_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("获取通知锁成功: {}", redisKey);
        return true;
    }

    /**
     * 构建 Redis Key
     * 格式: notify:{taskId}:{date}:{type}
     */
    private String buildRedisKey(Long taskId, LocalDate notifyDate, NotifyType notifyType) {
        return NOTIFY_KEY_PREFIX + taskId + ":" + notifyDate + ":" + notifyType.getCode();
    }
}
