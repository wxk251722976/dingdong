package com.dingdong.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dingdong.common.constant.RepeatType;
import com.dingdong.common.constant.TaskEnabled;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.checkin.ICheckInTaskService;
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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckInReminderTask {

    private final ICheckInTaskService checkInTaskService;
    private final ICheckInLogService checkInLogService;
    private final ISysUserService sysUserService;
    private final SubscribeMessageService subscribeMessageService;

    // 已发送提醒的任务缓存（避免重复发送）
    // key: taskId-日期, value: 发送时间
    private final Map<String, LocalDateTime> sentRemindCache = new HashMap<>();

    // 已发送漏打卡通知的任务缓存
    private final Map<String, LocalDateTime> sentMissedCache = new HashMap<>();

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

        // 清理过期缓存（只保留今天的记录）
        cleanExpiredCache(today);

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

        String cacheKey = task.getId() + "-" + today;
        boolean isCompleted = completedTaskIds.contains(task.getId());

        // 情况1: 到达叮咚时间 - 通知被叮咚者
        if (isTimeToRemind(now, targetTime) && !sentRemindCache.containsKey(cacheKey)) {
            sendRemindNotification(task, targetTime, userMap);
            sentRemindCache.put(cacheKey, now);
        }

        // 情况2: 超过30分钟未打卡 - 通知监督者（漏打卡）
        LocalDateTime missedTime = targetTime.plusMinutes(30);
        if (!isCompleted && now.isAfter(missedTime) && !sentMissedCache.containsKey(cacheKey)) {
            sendMissedNotification(task, targetTime, userMap);
            sentMissedCache.put(cacheKey, now);
        }
    }

    /**
     * 判断是否到了提醒时间（在目标时间前后1分钟内）
     */
    private boolean isTimeToRemind(LocalDateTime now, LocalDateTime targetTime) {
        return !now.isBefore(targetTime) && now.isBefore(targetTime.plusMinutes(1));
    }

    /**
     * 发送叮咚提醒给被叮咚者
     */
    private void sendRemindNotification(CheckInTask task, LocalDateTime remindTime, Map<Long, SysUser> userMap) {
        SysUser targetUser = userMap.get(task.getUserId());
        SysUser creator = task.getCreatorId() != null ? userMap.get(task.getCreatorId()) : null;

        if (targetUser == null || targetUser.getOpenid() == null) {
            log.warn("无法发送提醒：目标用户不存在或无openid, taskId={}", task.getId());
            return;
        }

        String supervisorName = creator != null ? creator.getNickname() : "系统";

        log.info("发送叮咚提醒: taskId={}, userId={}, title={}",
                task.getId(), task.getUserId(), task.getTitle());

        subscribeMessageService.sendRemindMessage(
                targetUser.getOpenid(),
                task.getTitle(),
                remindTime,
                supervisorName);
    }

    /**
     * 发送漏打卡通知给监督者
     */
    private void sendMissedNotification(CheckInTask task, LocalDateTime remindTime, Map<Long, SysUser> userMap) {
        if (task.getCreatorId() == null) {
            return;
        }

        SysUser supervisor = userMap.get(task.getCreatorId());
        SysUser supervised = userMap.get(task.getUserId());

        if (supervisor == null || supervisor.getOpenid() == null) {
            log.warn("无法发送漏打卡通知：监督者不存在或无openid, taskId={}", task.getId());
            return;
        }

        String supervisedName = supervised != null ? supervised.getNickname() : "未知用户";

        log.info("发送漏打卡通知: taskId={}, supervisorId={}, supervisedName={}",
                task.getId(), task.getCreatorId(), supervisedName);

        subscribeMessageService.sendMissedCheckInMessage(
                supervisor.getOpenid(),
                supervisedName,
                task.getTitle(),
                remindTime);
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

    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache(LocalDate today) {
        String todayPrefix = "-" + today;
        sentRemindCache.entrySet().removeIf(entry -> !entry.getKey().endsWith(todayPrefix));
        sentMissedCache.entrySet().removeIf(entry -> !entry.getKey().endsWith(todayPrefix));
    }
}
