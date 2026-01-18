package com.dingdong.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dingdong.common.constant.NotifyType;
import com.dingdong.common.constant.RepeatType;
import com.dingdong.common.constant.TaskEnabled;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.checkin.ICheckInTaskService;
import com.dingdong.service.notification.INotificationLogService;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.wechat.SubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 叮咚提醒定时任务
 * 负责在指定时间发送订阅消息通知
 * 
 * 使用 Redis + 数据库双保险机制防止重复推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckInReminderTask {

    private final ICheckInTaskService checkInTaskService;
    private final ICheckInLogService checkInLogService;
    private final ISysUserService sysUserService;
    private final SubscribeMessageService subscribeMessageService;
    private final INotificationLogService notificationLogService;

    /**
     * 每分钟执行一次，检查是否有需要发送的提醒
     * - 到达叮咚时间：通知被叮咚者
     * - 超过30分钟未打卡：通知监督者（漏打卡）
     * - 完成打卡：在打卡时实时通知（不在此任务处理）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndSendReminders() {
        log.debug("开始执行叮咚提醒检查任务...");

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = today.getDayOfWeek().getValue();

        // 1. 获取今天所有启用的任务
        List<CheckInTask> allTasks = checkInTaskService.list(
                new LambdaQueryWrapper<CheckInTask>()
                        .eq(CheckInTask::getStatus, TaskEnabled.ENABLED.getCode()));

        // 2. 过滤出今天生效的任务
        List<CheckInTask> activeTasks = allTasks.stream()
                .filter(task -> isTaskActiveOnDate(task, today, dayOfWeek))
                .toList();

        if (activeTasks.isEmpty()) {
            log.debug("今天没有需要处理的任务");
            return;
        }

        // 3. 批量获取用户信息
        Set<Long> userIds = new HashSet<>();
        activeTasks.forEach(task -> {
            userIds.add(task.getUserId());
            if (task.getCreatorId() != null) {
                userIds.add(task.getCreatorId());
            }
        });
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 4. 获取今天的打卡记录
        List<Long> taskIds = activeTasks.stream().map(CheckInTask::getId).toList();
        List<CheckInLog> todayLogs = checkInLogService.list(
                new LambdaQueryWrapper<CheckInLog>()
                        .in(CheckInLog::getTaskId, taskIds)
                        .ge(CheckInLog::getCheckTime, today.atStartOfDay())
                        .lt(CheckInLog::getCheckTime, today.plusDays(1).atStartOfDay()));

        Set<Long> completedTaskIds = todayLogs.stream()
                .map(CheckInLog::getTaskId)
                .collect(Collectors.toSet());

        // 5. 处理每个任务
        for (CheckInTask task : activeTasks) {
            processTask(task, today, now, userMap, completedTaskIds);
        }
    }

    /**
     * 处理单个任务的提醒逻辑
     */
    private void processTask(CheckInTask task, LocalDate today, LocalDateTime now,
            Map<Long, SysUser> userMap, Set<Long> completedTaskIds) {
        if (task.getRemindTime() == null) {
            return;
        }

        // 计算今天的目标提醒时间
        LocalDateTime targetTime;
        if (RepeatType.ONCE.getCode().equals(task.getRepeatType())) {
            targetTime = task.getRemindTime();
        } else {
            targetTime = LocalDateTime.of(today, task.getRemindTime().toLocalTime());
        }

        boolean isCompleted = completedTaskIds.contains(task.getId());

        // 情况1: 到达叮咚时间 (提前30分钟任务提醒) - 通知被叮咚者
        LocalDateTime remindTriggerTime = targetTime.minusMinutes(30);
        if (isTimeToRemind(now, remindTriggerTime)) {
            sendRemindNotificationWithCheck(task, today, targetTime, userMap);
        }

        // 情况2: 超过30分钟未打卡 - 通知监督者（漏打卡）
        LocalDateTime missedTime = targetTime.plusMinutes(30);
        if (!isCompleted && now.isAfter(missedTime)) {
            sendMissedNotificationWithCheck(task, today, targetTime, userMap);
        }
    }

    /**
     * 判断是否到了提醒时间（在目标时间前后1分钟内）
     */
    private boolean isTimeToRemind(LocalDateTime now, LocalDateTime targetTime) {
        return !now.isBefore(targetTime) && now.isBefore(targetTime.plusMinutes(1));
    }

    /**
     * 发送叮咚提醒（带防重检查）
     */
    private void sendRemindNotificationWithCheck(CheckInTask task, LocalDate today,
            LocalDateTime remindTime, Map<Long, SysUser> userMap) {
        // Redis + 数据库双保险防重
        if (!notificationLogService.tryAcquireNotifyLock(task.getId(), today, NotifyType.REMIND)) {
            log.debug("叮咚提醒已发送过: taskId={}, date={}", task.getId(), today);
            return;
        }

        SysUser targetUser = userMap.get(task.getUserId());
        SysUser creator = task.getCreatorId() != null ? userMap.get(task.getCreatorId()) : null;

        if (targetUser == null || targetUser.getOpenid() == null) {
            log.warn("无法发送提醒：目标用户不存在或无openid, taskId={}", task.getId());
            notificationLogService.record(task.getId(), today, NotifyType.REMIND,
                    task.getUserId(), false, "目标用户不存在或无openid");
            return;
        }

        String supervisorName = creator != null ? creator.getNickname() : "系统";

        log.info("发送叮咚提醒: taskId={}, userId={}, title={}",
                task.getId(), task.getUserId(), task.getTitle());

        try {
            subscribeMessageService.sendRemindMessage(
                    targetUser.getOpenid(),
                    task.getTitle(),
                    remindTime,
                    supervisorName);
            // 记录发送成功
            notificationLogService.record(task.getId(), today, NotifyType.REMIND,
                    task.getUserId(), true, null);
        } catch (Exception e) {
            log.error("叮咚提醒发送失败: taskId={}", task.getId(), e);
            notificationLogService.record(task.getId(), today, NotifyType.REMIND,
                    task.getUserId(), false, e.getMessage());
        }
    }

    /**
     * 发送漏打卡通知（带防重检查）
     */
    private void sendMissedNotificationWithCheck(CheckInTask task, LocalDate today,
            LocalDateTime remindTime, Map<Long, SysUser> userMap) {
        if (task.getCreatorId() == null) {
            return;
        }

        // Redis + 数据库双保险防重
        if (!notificationLogService.tryAcquireNotifyLock(task.getId(), today, NotifyType.MISSED)) {
            log.debug("漏打卡通知已发送过: taskId={}, date={}", task.getId(), today);
            return;
        }

        SysUser supervisor = userMap.get(task.getCreatorId());
        SysUser supervised = userMap.get(task.getUserId());

        if (supervisor == null || supervisor.getOpenid() == null) {
            log.warn("无法发送漏打卡通知：监督者不存在或无openid, taskId={}", task.getId());
            notificationLogService.record(task.getId(), today, NotifyType.MISSED,
                    task.getCreatorId(), false, "监督者不存在或无openid");
            return;
        }

        String supervisedName = supervised != null ? supervised.getNickname() : "未知用户";

        log.info("发送漏打卡通知: taskId={}, supervisorId={}, supervisedName={}",
                task.getId(), task.getCreatorId(), supervisedName);

        try {
            subscribeMessageService.sendMissedCheckInMessage(
                    supervisor.getOpenid(),
                    supervisedName,
                    task.getTitle(),
                    remindTime);
            // 记录发送成功
            notificationLogService.record(task.getId(), today, NotifyType.MISSED,
                    task.getCreatorId(), true, null);
        } catch (Exception e) {
            log.error("漏打卡通知发送失败: taskId={}", task.getId(), e);
            notificationLogService.record(task.getId(), today, NotifyType.MISSED,
                    task.getCreatorId(), false, e.getMessage());
        }
    }

    /**
     * 判断任务在指定日期是否生效
     */
    private boolean isTaskActiveOnDate(CheckInTask task, LocalDate date, int dayOfWeek) {
        RepeatType repeatType = RepeatType.fromCode(task.getRepeatType());
        if (repeatType == null) {
            return false;
        }

        if (repeatType == RepeatType.ONCE) {
            LocalDateTime remindTime = task.getRemindTime();
            return remindTime != null && remindTime.toLocalDate().equals(date);
        } else {
            return repeatType.matchesDayOfWeek(dayOfWeek);
        }
    }
}
