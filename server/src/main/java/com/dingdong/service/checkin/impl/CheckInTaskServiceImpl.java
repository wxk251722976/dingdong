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
import com.dingdong.service.wechat.SubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 叮咚任务服务实现类
 * 
 * @author Antigravity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInTaskServiceImpl extends ServiceImpl<CheckInTaskMapper, CheckInTask> implements ICheckInTaskService {

    private final ICheckInLogService checkInLogService;
    private final IUserRelationService userRelationService;
    private final ISysUserService sysUserService;
    private final SubscribeMessageService subscribeMessageService;

    @Override
    public List<DailyTaskStatusDTO> getDailyTaskStatus(Long userId, LocalDate date) {
        return getDailyTaskStatusByCreator(userId, date, null);
    }

    @Override
    public List<DailyTaskStatusDTO> getDailyTaskStatusByCreator(Long userId, LocalDate date, Long creatorId) {
        // 1. 查询用户启用状态的任务
        List<CheckInTask> allTasks = listEnabledTasks(userId, creatorId);

        // 2. 过滤当天生效的任务
        List<CheckInTask> activeTasks = filterActiveTasks(allTasks, date);
        if (activeTasks.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 查询当天的打卡记录
        Map<Long, CheckInLog> logMap = queryTaskLogs(activeTasks, userId, date);

        // 4. 组装DTO
        boolean isToday = date.equals(LocalDate.now());
        return activeTasks.stream().map(task -> buildDailyTaskStatusDTO(task, logMap.get(task.getId()), isToday, date))
                .collect(Collectors.toList());
    }

    @Override
    public List<SupervisedUserStatusDTO> getSupervisedUserStatusList(Long userId) {
        // 1. 获取已接受的关系（平等关系，用户可能是发起人或伙伴）
        List<UserRelation> relations = getAcceptedRelations(userId);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取关系中的对方用户ID
        List<Long> partnerUserIds = relations.stream()
                .map(r -> r.getInitiatorId().equals(userId) ? r.getPartnerId() : r.getInitiatorId())
                .collect(Collectors.toList());
        Map<Long, SysUser> userMap = getUserMap(partnerUserIds);

        LocalDate today = LocalDate.now();

        // 2. 获取所有关系伙伴的今日生效任务（由当前用户创建的）
        List<CheckInTask> activeTasks = getActiveTasksForUsers(partnerUserIds, userId, today);

        // 如果没有任何任务，直接返回所有用户状态为 PENDING
        if (activeTasks.isEmpty()) {
            return buildResultWithStatus(relations, userId, userMap, TaskStatus.PENDING.getCode());
        }

        // 3. 获取任务的打卡记录
        Map<Long, CheckInLog> logMap = queryTaskLogs(activeTasks, today);

        // 4. 组装结果
        Map<Long, List<CheckInTask>> tasksByUserId = activeTasks.stream()
                .collect(Collectors.groupingBy(CheckInTask::getUserId));

        return relations.stream()
                .map(relation -> {
                    Long partnerId = relation.getInitiatorId().equals(userId) ? relation.getPartnerId()
                            : relation.getInitiatorId();
                    return buildSupervisedUserStatusDTO(relation, userMap.get(partnerId), tasksByUserId, logMap, today);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean createTask(TaskDTO taskDTO) {
        validateTaskRelation(taskDTO);

        CheckInTask task = BeanUtil.toBean(taskDTO, CheckInTask.class);
        task.setStatus(TaskEnabled.ENABLED.getCode());
        boolean saved = this.save(task);

        if (saved) {
            notifyTaskExecutor(taskDTO, task);
        }
        return saved;
    }

    /**
     * 验证任务创建者与执行者的关系
     */
    private void validateTaskRelation(TaskDTO taskDTO) {
        if (!taskDTO.getCreatorId().equals(taskDTO.getUserId())) {
            boolean isRelated = userRelationService.checkRelation(taskDTO.getCreatorId(), taskDTO.getUserId());
            if (!isRelated) {
                throw new RuntimeException("无法创建任务：您与目标用户未绑定关系");
            }
        }
    }

    /**
     * 通知任务执行者有新任务
     */
    private void notifyTaskExecutor(TaskDTO taskDTO, CheckInTask task) {
        // 仅在给他人设置任务时发送通知
        if (taskDTO.getCreatorId().equals(taskDTO.getUserId())) {
            return;
        }

        try {
            SysUser executor = sysUserService.getById(taskDTO.getUserId());
            if (executor == null || executor.getOpenid() == null) {
                log.debug("跳过通知: 执行者不存在或无openid");
                return;
            }

            SysUser creator = sysUserService.getById(taskDTO.getCreatorId());
            String creatorName = (creator != null) ? creator.getNickname() : "用户";

            subscribeMessageService.sendTaskReminderMessage(
                    executor.getOpenid(),
                    task.getTitle(),
                    task.getRemindTime(),
                    creatorName);
        } catch (Exception e) {
            log.warn("发送任务设置通知失败: {}", e.getMessage());
        }
    }

    // ================== 私有辅助方法 ==================

    private List<CheckInTask> listEnabledTasks(Long userId, Long creatorId) {
        LambdaQueryWrapper<CheckInTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInTask::getUserId, userId)
                .eq(CheckInTask::getStatus, TaskEnabled.ENABLED.getCode());
        if (creatorId != null) {
            wrapper.eq(CheckInTask::getCreatorId, creatorId);
        }
        wrapper.orderByAsc(CheckInTask::getRemindTime);
        return this.list(wrapper);
    }

    private List<CheckInTask> filterActiveTasks(List<CheckInTask> tasks, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return tasks.stream()
                .filter(task -> isTaskActiveOnDate(task, date, dayOfWeek))
                .collect(Collectors.toList());
    }

    private Map<Long, CheckInLog> queryTaskLogs(List<CheckInTask> tasks, Long userId, LocalDate date) {
        if (tasks.isEmpty())
            return Collections.emptyMap();
        List<Long> taskIds = tasks.stream().map(CheckInTask::getId).collect(Collectors.toList());

        List<CheckInLog> logs = checkInLogService.list(new LambdaQueryWrapper<CheckInLog>()
                .eq(CheckInLog::getUserId, userId)
                .in(CheckInLog::getTaskId, taskIds)
                .ge(CheckInLog::getCheckTime, date.atStartOfDay())
                .lt(CheckInLog::getCheckTime, date.plusDays(1).atStartOfDay()));

        return logs.stream()
                .filter(l -> l.getTaskId() != null)
                .collect(Collectors.toMap(CheckInLog::getTaskId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, CheckInLog> queryTaskLogs(List<CheckInTask> tasks, LocalDate date) {
        if (tasks.isEmpty())
            return Collections.emptyMap();
        List<Long> taskIds = tasks.stream().map(CheckInTask::getId).collect(Collectors.toList());

        List<CheckInLog> logs = checkInLogService.list(new LambdaQueryWrapper<CheckInLog>()
                .in(CheckInLog::getTaskId, taskIds)
                .ge(CheckInLog::getCheckTime, date.atStartOfDay())
                .lt(CheckInLog::getCheckTime, date.plusDays(1).atStartOfDay()));

        return logs.stream()
                .filter(l -> l.getTaskId() != null)
                .collect(Collectors.toMap(CheckInLog::getTaskId, Function.identity(), (a, b) -> a));
    }

    private DailyTaskStatusDTO buildDailyTaskStatusDTO(CheckInTask task, CheckInLog log, boolean isToday,
            LocalDate date) {
        DailyTaskStatusDTO dto = new DailyTaskStatusDTO();
        dto.setTaskId(task.getId());
        dto.setTitle(task.getTitle());

        // 计算当天的提醒时间
        LocalDateTime remindTime = task.getRemindTime();
        if (remindTime != null && !RepeatType.ONCE.getCode().equals(task.getRepeatType())) {
            remindTime = LocalDateTime.of(date, remindTime.toLocalTime());
        }
        dto.setRemindTime(remindTime);

        dto.setRepeatType(task.getRepeatType());
        dto.setStatus(calculateTaskStatus(task, log, isToday, date));
        dto.setCheckTime(log != null ? log.getCheckTime() : null);
        return dto;
    }

    private List<UserRelation> getAcceptedRelations(Long userId) {
        // 查询用户参与的所有已接受的关系（可能是发起人或伙伴）
        return userRelationService.list(new LambdaQueryWrapper<UserRelation>()
                .and(w -> w.eq(UserRelation::getInitiatorId, userId).or().eq(UserRelation::getPartnerId, userId))
                .eq(UserRelation::getStatus, RelationStatus.ACCEPTED.getCode()));
    }

    private Map<Long, SysUser> getUserMap(List<Long> userIds) {
        if (userIds.isEmpty())
            return Collections.emptyMap();
        return sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private List<CheckInTask> getActiveTasksForUsers(List<Long> userIds, Long creatorId, LocalDate date) {
        List<CheckInTask> allTasks = this.list(new LambdaQueryWrapper<CheckInTask>()
                .in(CheckInTask::getUserId, userIds)
                .eq(CheckInTask::getCreatorId, creatorId)
                .eq(CheckInTask::getStatus, TaskEnabled.ENABLED.getCode()));
        return filterActiveTasks(allTasks, date);
    }

    private SupervisedUserStatusDTO buildSupervisedUserStatusDTO(UserRelation relation, SysUser user,
            Map<Long, List<CheckInTask>> tasksByUserId, Map<Long, CheckInLog> logMap, LocalDate today) {
        if (user == null)
            return null;

        SupervisedUserStatusDTO dto = new SupervisedUserStatusDTO();
        dto.setUserId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setRelationName(relation.getRelationName());

        List<CheckInTask> userTasks = tasksByUserId.getOrDefault(user.getId(), Collections.emptyList());
        if (userTasks.isEmpty()) {
            dto.setTodayStatus(TaskStatus.PENDING.getCode());
        } else {
            List<Integer> statuses = userTasks.stream()
                    .map(task -> calculateTaskStatus(task, logMap.get(task.getId()), true, today))
                    .collect(Collectors.toList());
            dto.setTodayStatus(aggregateStatuses(statuses));
        }
        return dto;
    }

    private List<SupervisedUserStatusDTO> buildResultWithStatus(
            List<UserRelation> relations, Long currentUserId, Map<Long, SysUser> userMap, Integer status) {
        return relations.stream().map(relation -> {
            Long partnerId = relation.getInitiatorId().equals(currentUserId) ? relation.getPartnerId()
                    : relation.getInitiatorId();
            SysUser user = userMap.get(partnerId);
            if (user == null)
                return null;

            SupervisedUserStatusDTO dto = new SupervisedUserStatusDTO();
            dto.setUserId(user.getId());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
            dto.setRelationName(relation.getRelationName());
            dto.setTodayStatus(status);
            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean isTaskActiveOnDate(CheckInTask task, LocalDate date, int dayOfWeek) {
        RepeatType repeatType = RepeatType.fromCode(task.getRepeatType());
        if (repeatType == null)
            return false;

        if (repeatType == RepeatType.ONCE) {
            LocalDateTime remindTime = task.getRemindTime();
            return remindTime != null && remindTime.toLocalDate().equals(date);
        } else {
            return repeatType.matchesDayOfWeek(dayOfWeek);
        }
    }

    private Integer calculateTaskStatus(CheckInTask task, CheckInLog checkInLog, boolean isToday, LocalDate date) {
        if (checkInLog != null) {
            return checkInLog.getStatus();
        }

        if (date.isBefore(LocalDate.now())) {
            return TaskStatus.MISSED.getCode();
        }

        if (isToday) {
            LocalDateTime remindTime = task.getRemindTime();
            if (remindTime == null)
                return TaskStatus.PENDING.getCode();

            LocalDateTime targetTime;
            if (RepeatType.ONCE.getCode().equals(task.getRepeatType())) {
                targetTime = remindTime;
            } else {
                targetTime = LocalDateTime.of(date, remindTime.toLocalTime());
            }

            // 允许打卡时限：30分钟
            if (LocalDateTime.now().isAfter(targetTime.plusMinutes(30))) {
                return TaskStatus.MISSED.getCode();
            }
        }

        return TaskStatus.PENDING.getCode();
    }

    private Integer aggregateStatuses(List<Integer> statuses) {
        if (statuses.isEmpty())
            return TaskStatus.PENDING.getCode();

        if (statuses.stream().anyMatch(s -> TaskStatus.MISSED.getCode().equals(s))) {
            return TaskStatus.MISSED.getCode();
        }

        if (statuses.stream()
                .allMatch(s -> TaskStatus.NORMAL.getCode().equals(s) || TaskStatus.LATE.getCode().equals(s))) {
            return TaskStatus.NORMAL.getCode();
        }

        return TaskStatus.PENDING.getCode();
    }
}
