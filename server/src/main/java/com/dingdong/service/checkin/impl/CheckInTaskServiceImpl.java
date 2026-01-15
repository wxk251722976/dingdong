package com.dingdong.service.checkin.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.RelationStatus;
import com.dingdong.common.constant.RepeatType;
import com.dingdong.common.constant.TaskEnabled;
import com.dingdong.common.constant.TaskStatus;
import com.dingdong.dto.checkin.DailyTaskStatusDTO;
import com.dingdong.dto.checkin.SupervisedUserStatusDTO;
import com.dingdong.dto.checkin.TaskDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.mapper.checkin.CheckInTaskMapper;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.checkin.ICheckInTaskService;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.user.IUserRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 叮咚任务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInTaskServiceImpl extends ServiceImpl<CheckInTaskMapper, CheckInTask> implements ICheckInTaskService {

    private final ICheckInLogService checkInLogService;
    private final IUserRelationService userRelationService;
    private final ISysUserService sysUserService;

    /**
     * 获取用户每日任务及完成状态（不限制创建者）
     * 主要供被监督者自己查看使用
     */
    @Override
    public List<DailyTaskStatusDTO> getDailyTaskStatus(Long userId, LocalDate date) {
        return getDailyTaskStatusByCreator(userId, date, null);
    }

    /**
     * 获取用户每日任务及完成状态（限定创建者）
     * 
     * @param userId    被监督者用户ID
     * @param date      日期
     * @param creatorId 创建者ID（监督者），如果为null则不限制
     * @return 任务状态列表
     */
    public List<DailyTaskStatusDTO> getDailyTaskStatusByCreator(Long userId, LocalDate date, Long creatorId) {
        // 1. 查询用户启用状态的任务
        LambdaQueryWrapper<CheckInTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInTask::getUserId, userId)
                .eq(CheckInTask::getStatus, TaskEnabled.ENABLED.getCode());

        if (creatorId != null) {
            wrapper.eq(CheckInTask::getCreatorId, creatorId);
        }

        wrapper.orderByAsc(CheckInTask::getRemindTime);
        List<CheckInTask> allTasks = this.list(wrapper);

        // 2. 根据重复类型过滤当天生效的任务
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<CheckInTask> tasks = allTasks.stream()
                .filter(task -> isTaskActiveOnDate(task, date, dayOfWeek))
                .toList();

        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 查询当天的打卡记录
        List<Long> taskIds = tasks.stream().map(CheckInTask::getId).collect(Collectors.toList());
        LambdaQueryWrapper<CheckInLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(CheckInLog::getUserId, userId)
                .in(CheckInLog::getTaskId, taskIds)
                .ge(CheckInLog::getCheckTime, date.atStartOfDay())
                .lt(CheckInLog::getCheckTime, date.plusDays(1).atStartOfDay());

        List<CheckInLog> logs = checkInLogService.list(logWrapper);

        Map<Long, CheckInLog> logMap = logs.stream()
                .filter(l -> l.getTaskId() != null)
                .collect(Collectors.toMap(CheckInLog::getTaskId, Function.identity(), (a, b) -> a));

        // 4. 组装DTO
        boolean isToday = date.equals(LocalDate.now());

        return tasks.stream().map(task -> {
            DailyTaskStatusDTO dto = new DailyTaskStatusDTO();
            dto.setTaskId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setRemindTime(task.getRemindTime());
            dto.setRepeatType(task.getRepeatType());

            CheckInLog checkInLog = logMap.get(task.getId());
            dto.setStatus(calculateTaskStatus(task, checkInLog, isToday, date));
            dto.setCheckTime(checkInLog != null ? checkInLog.getCheckTime() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取监督用户的今日状态列表（优化版：无循环查询）
     * 
     * 查询次数优化：
     * 1. 查询关系表 - 1次
     * 2. 查询用户表 - 1次
     * 3. 查询任务表 - 1次
     * 4. 查询打卡记录表 - 1次
     * 总计: 4次数据库查询，与被监督者数量无关
     */
    @Override
    public List<SupervisedUserStatusDTO> getSupervisedUserStatusList(Long supervisorId) {
        // ==================== 第1次查询：获取已接受的监督关系 ====================
        List<UserRelation> relations = userRelationService.list(
                new LambdaQueryWrapper<UserRelation>()
                        .eq(UserRelation::getSupervisorId, supervisorId)
                        .eq(UserRelation::getStatus, RelationStatus.ACCEPTED.getCode()));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取被监督者ID列表
        List<Long> supervisedUserIds = relations.stream()
                .map(UserRelation::getSupervisedId)
                .collect(Collectors.toList());

        // ==================== 第2次查询：批量获取被监督者用户信息 ====================
        Map<Long, SysUser> userMap = sysUserService.listByIds(supervisedUserIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue();

        // ==================== 第3次查询：批量获取所有被监督者的任务（由当前监督者创建） ====================
        List<CheckInTask> allTasks = this.list(
                new LambdaQueryWrapper<CheckInTask>()
                        .in(CheckInTask::getUserId, supervisedUserIds)
                        .eq(CheckInTask::getCreatorId, supervisorId)
                        .eq(CheckInTask::getStatus, TaskEnabled.ENABLED.getCode()));

        // 过滤出今天生效的任务
        List<CheckInTask> activeTasks = allTasks.stream()
                .filter(task -> isTaskActiveOnDate(task, today, dayOfWeek))
                .toList();

        // 如果没有任何任务，直接返回所有用户状态为 PENDING
        if (activeTasks.isEmpty()) {
            return buildResultWithStatus(relations, userMap, TaskStatus.PENDING.getCode());
        }

        // 按userId分组任务
        Map<Long, List<CheckInTask>> tasksByUserId = activeTasks.stream()
                .collect(Collectors.groupingBy(CheckInTask::getUserId));

        // 提取所有任务ID
        List<Long> taskIds = activeTasks.stream()
                .map(CheckInTask::getId)
                .collect(Collectors.toList());

        // ==================== 第4次查询：批量获取所有任务的今日打卡记录 ====================
        List<CheckInLog> allLogs = checkInLogService.list(
                new LambdaQueryWrapper<CheckInLog>()
                        .in(CheckInLog::getTaskId, taskIds)
                        .ge(CheckInLog::getCheckTime, today.atStartOfDay())
                        .lt(CheckInLog::getCheckTime, today.plusDays(1).atStartOfDay()));

        // 按taskId分组打卡记录
        Map<Long, CheckInLog> logMap = allLogs.stream()
                .filter(l -> l.getTaskId() != null)
                .collect(Collectors.toMap(CheckInLog::getTaskId, Function.identity(), (a, b) -> a));

        // ==================== 内存计算：组装每个被监督者的状态 ====================
        return relations.stream().map(relation -> {
            SysUser user = userMap.get(relation.getSupervisedId());
            if (user == null) {
                return null;
            }

            SupervisedUserStatusDTO dto = new SupervisedUserStatusDTO();
            dto.setUserId(user.getId());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            dto.setRelationName(relation.getRelationName());

            // 获取该用户的任务
            List<CheckInTask> userTasks = tasksByUserId.getOrDefault(user.getId(), Collections.emptyList());

            if (userTasks.isEmpty()) {
                dto.setTodayStatus(TaskStatus.PENDING.getCode());
                return dto;
            }

            // 计算每个任务的状态
            List<Integer> statuses = userTasks.stream()
                    .map(task -> {
                        CheckInLog checkInLog = logMap.get(task.getId());
                        return calculateTaskStatus(task, checkInLog, true, today);
                    })
                    .collect(Collectors.toList());

            // 聚合状态
            dto.setTodayStatus(aggregateStatuses(statuses));

            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 构建带有统一状态的结果列表（辅助方法）
     */
    private List<SupervisedUserStatusDTO> buildResultWithStatus(
            List<UserRelation> relations,
            Map<Long, SysUser> userMap,
            Integer status) {
        return relations.stream().map(relation -> {
            SysUser user = userMap.get(relation.getSupervisedId());
            if (user == null) {
                return null;
            }

            SupervisedUserStatusDTO dto = new SupervisedUserStatusDTO();
            dto.setUserId(user.getId());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            dto.setRelationName(relation.getRelationName());
            dto.setTodayStatus(status);
            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 聚合状态列表
     */
    private Integer aggregateStatuses(List<Integer> statuses) {
        if (statuses.isEmpty()) {
            return TaskStatus.PENDING.getCode();
        }

        boolean anyMissed = statuses.stream()
                .anyMatch(s -> TaskStatus.MISSED.getCode().equals(s));

        if (anyMissed) {
            return TaskStatus.MISSED.getCode();
        }

        boolean allCompleted = statuses.stream()
                .allMatch(s -> TaskStatus.NORMAL.getCode().equals(s) ||
                        TaskStatus.LATE.getCode().equals(s));

        if (allCompleted) {
            return TaskStatus.NORMAL.getCode();
        }

        return TaskStatus.PENDING.getCode();
    }

    @Override
    public boolean createTask(TaskDTO taskDTO) {
        CheckInTask task = BeanUtil.toBean(taskDTO, CheckInTask.class);
        task.setStatus(TaskEnabled.ENABLED.getCode());
        return this.save(task);
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
     * 计算单条任务的状态
     * 
     * 状态计算规则：
     * 1. 有打卡记录 -> 返回打卡记录的状态（NORMAL 或 LATE）
     * 2. 无打卡记录 + 过去日期 -> MISSED
     * 3. 无打卡记录 + 今天 + 超过限制时间 -> MISSED
     * 4. 其他情况 -> PENDING
     */
    private Integer calculateTaskStatus(CheckInTask task, CheckInLog checkInLog, boolean isToday, LocalDate date) {

        // ★ 关键：如果有打卡记录，直接返回打卡状态
        if (checkInLog != null) {
            log.debug("任务[{}]已打卡，状态={}", task.getId(), checkInLog.getStatus());
            return checkInLog.getStatus();
        }

        // 以下是【无打卡记录】的情况

        // 如果是过去日期且无记录，则为错过
        if (date.isBefore(LocalDate.now())) {
            return TaskStatus.MISSED.getCode();
        }

        // 如果是今天，检查是否超时
        if (isToday) {
            LocalDateTime remindTime = task.getRemindTime();
            if (remindTime == null) {
                return TaskStatus.PENDING.getCode();
            }

            LocalDateTime targetTime;
            if (RepeatType.ONCE.getCode().equals(task.getRepeatType())) {
                targetTime = remindTime;
            } else {
                targetTime = LocalDateTime.of(date, remindTime.toLocalTime());
            }

            // 允许打卡时限：提醒时间 + 30分钟
            LocalDateTime limitTime = targetTime.plusMinutes(30);

            // 无打卡记录 + 超过限制时间 = 错过
            if (LocalDateTime.now().isAfter(limitTime)) {
                return TaskStatus.MISSED.getCode();
            }
        }

        return TaskStatus.PENDING.getCode();
    }
}
