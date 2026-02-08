package com.dingdong.service.checkin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.CheckInStatus;
import com.dingdong.common.constant.NotifyType;
import com.dingdong.common.constant.RepeatType;
import com.dingdong.common.exception.ServiceException;
import com.dingdong.dto.checkin.CheckInDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.entity.user.SysUser;
import com.dingdong.mapper.checkin.CheckInLogMapper;
import com.dingdong.mapper.checkin.CheckInTaskMapper;
import com.dingdong.service.checkin.CheckInBitmapService;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.notification.INotificationLogService;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.wechat.SubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 打卡日志业务实现
 * 
 * @author Antigravity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInLogServiceImpl extends ServiceImpl<CheckInLogMapper, CheckInLog> implements ICheckInLogService {

    private final CheckInTaskMapper checkInTaskMapper;
    private final ISysUserService sysUserService;
    private final SubscribeMessageService subscribeMessageService;
    private final CheckInBitmapService checkInBitmapService;
    private final INotificationLogService notificationLogService;
    private final Executor businessExecutor;

    @Override
    public boolean doCheckIn(CheckInDTO checkInDTO) {
        // 1. 获取并验证任务
        CheckInTask task = getAndValidateTask(checkInDTO.getTaskId());

        // 2. 计算并验证打卡时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = calculateTargetTime(task);
        validateCheckInTime(now, targetTime);

        // 3. 构建并保存打卡记录
        CheckInLog checkInLog = createCheckInLog(checkInDTO, now, targetTime);
        boolean saved = this.save(checkInLog);

        // 4. 异步执行后置处理（Redis统计 + 消息通知）
        if (saved) {
            CompletableFuture
                    .runAsync(() -> processAfterCheckIn(checkInDTO, task, now, targetTime, checkInLog.getUserId()),
                            businessExecutor)
                    .exceptionally(e -> {
                        log.error("打卡后置处理执行异常", e);
                        return null;
                    });
        }

        return saved;
    }

    /**
     * 打卡后置处理（Redis记录 + 消息通知）
     * 聚合辅助业务逻辑，统一异常边界
     */
    private void processAfterCheckIn(CheckInDTO checkInDTO, CheckInTask task, LocalDateTime now,
            LocalDateTime targetTime, Long userId) {
        // 1. 记录到 Redis Bitmap
        try {
            checkInBitmapService.recordTaskCheckIn(
                    userId,
                    checkInDTO.getTaskId(),
                    now.toLocalDate());
        } catch (Exception e) {
            log.warn("Redis Bitmap 记录失败: userId={}, taskId={}", userId, checkInDTO.getTaskId(), e);
        }

        // 2. 发送通知
        if (task.getCreatorId() != null) {
            try {
                boolean isOnTime = !now.isAfter(targetTime);
                sendCheckInNotification(task, userId, now, isOnTime);
            } catch (Exception e) {
                log.error("发送打卡通知失败: userId={}, taskId={}", userId, task.getId(), e);
            }
        }
    }

    /**
     * 获取并验证任务是否存在
     */
    private CheckInTask getAndValidateTask(Long taskId) {
        CheckInTask task = checkInTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        return task;
    }

    /**
     * 计算当次打卡的目标时间
     */
    private LocalDateTime calculateTargetTime(CheckInTask task) {
        if (task.getRemindTime() == null) {
            throw new ServiceException("任务时间未设置");
        }

        if (RepeatType.ONCE.getCode().equals(task.getRepeatType())) {
            return task.getRemindTime();
        } else {
            // 对于重复任务，取当天的提醒时间
            return LocalDateTime.of(LocalDate.now(), task.getRemindTime().toLocalTime());
        }
    }

    /**
     * 验证打卡时间是否有效（在目标时间前后30分钟内）
     */
    private void validateCheckInTime(LocalDateTime now, LocalDateTime targetTime) {
        int limitMinutes = 30;
        LocalDateTime startTime = targetTime.minusMinutes(limitMinutes);
        LocalDateTime endTime = targetTime.plusMinutes(limitMinutes);

        if (now.isBefore(startTime)) {
            throw new ServiceException("还未到打卡时间哦");
        }

        if (now.isAfter(endTime)) {
            throw new ServiceException("任务已过期，无法打卡");
        }
    }

    /**
     * 构建打卡日志实体
     */
    private CheckInLog createCheckInLog(CheckInDTO checkInDTO, LocalDateTime now, LocalDateTime targetTime) {
        CheckInLog checkInLog = new CheckInLog();
        checkInLog.setUserId(checkInDTO.getUserId());
        checkInLog.setTaskId(checkInDTO.getTaskId());
        checkInLog.setCheckTime(now);
        checkInLog.setCreateTime(now);

        // 判断打卡状态 (正常 vs 补打卡)
        boolean isOnTime = !now.isAfter(targetTime);
        checkInLog.setStatus(isOnTime ? CheckInStatus.NORMAL.getCode() : CheckInStatus.REISSUE.getCode());

        return checkInLog;
    }

    /**
     * 发送打卡完成通知
     * (由 processAfterCheckIn 调用，异常由上层统一处理)
     */
    private void sendCheckInNotification(CheckInTask task, Long userId, LocalDateTime checkTime,
            boolean isOnTime) {
        // 确定通知类型：正常打卡 or 补打卡
        NotifyType notifyType = isOnTime ? NotifyType.CHECK_IN_COMPLETE : NotifyType.MAKE_UP;
        LocalDate notifyDate = checkTime.toLocalDate();

        // 获取监督者信息
        SysUser supervisor = sysUserService.getById(task.getCreatorId());
        if (supervisor == null || supervisor.getOpenid() == null) {
            log.warn("监督者不存在或无openid，无法发送打卡完成通知: supervisorId={}", task.getCreatorId());
            notificationLogService.record(task.getId(), notifyDate, notifyType,
                    task.getCreatorId(), false, "监督者不存在或无openid");
            return;
        }

        // 获取被监督者信息
        SysUser supervised = sysUserService.getById(userId);
        String supervisedName = supervised != null ? supervised.getNickname() : "未知用户";

        log.info("发送打卡完成通知给监督者: supervisorId={}, supervisedName={}, taskTitle={}, isOnTime={}",
                task.getCreatorId(), supervisedName, task.getTitle(), isOnTime);

        try {
            subscribeMessageService.sendCheckInCompleteMessage(
                    supervisor.getOpenid(),
                    supervisedName,
                    task.getTitle(),
                    checkTime,
                    isOnTime);
            // 记录发送成功
            notificationLogService.record(task.getId(), notifyDate, notifyType,
                    task.getCreatorId(), true, null);
        } catch (Exception e) {
            log.error("发送打卡通知失败: taskId={}", task.getId(), e);
            notificationLogService.record(task.getId(), notifyDate, notifyType,
                    task.getCreatorId(), false, e.getMessage());
            throw e; // 重新抛出让上层捕获
        }
    }

    @Override
    public IPage<CheckInLog> getLogsByUserId(IPage<CheckInLog> page, Long userId) {
        return this.page(page, new LambdaQueryWrapper<CheckInLog>()
                .eq(CheckInLog::getUserId, userId)
                .orderByDesc(CheckInLog::getCheckTime));
    }
}
