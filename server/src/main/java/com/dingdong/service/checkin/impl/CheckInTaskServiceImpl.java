package com.dingdong.service.checkin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.RepeatType;
import com.dingdong.common.constant.TaskEnabled;
import com.dingdong.common.constant.TaskStatus;
import com.dingdong.dto.checkin.DailyTaskStatusDTO;
import com.dingdong.dto.checkin.TaskDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.mapper.checkin.CheckInTaskMapper;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.checkin.ICheckInTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 叮咚任务服务实现类
 */
@Service
@RequiredArgsConstructor
public class CheckInTaskServiceImpl extends ServiceImpl<CheckInTaskMapper, CheckInTask> implements ICheckInTaskService {

    private final ICheckInLogService checkInLogService;

    @Override
    public List<DailyTaskStatusDTO> getDailyTaskStatus(Long userId, LocalDate date) {
        // 1. 查询用户启用状态的任务
        LambdaQueryWrapper<CheckInTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInTask::getUserId, userId)
                .eq(CheckInTask::getStatus, TaskEnabled.ENABLED.getCode())
                .orderByAsc(CheckInTask::getRemindTime);
        List<CheckInTask> allTasks = this.list(wrapper);

        // 2. 根据重复类型过滤当天生效的任务
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<CheckInTask> tasks = allTasks.stream()
                .filter(task -> isTaskActiveOnDate(task, date, dayOfWeek))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            return new ArrayList<>();
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
                .filter(log -> log.getTaskId() != null)
                .collect(Collectors.toMap(CheckInLog::getTaskId, Function.identity(), (a, b) -> a));

        // 4. 组装DTO
        LocalTime nowTime = LocalTime.now();
        boolean isToday = date.equals(LocalDate.now());

        return tasks.stream().map(task -> {
            DailyTaskStatusDTO dto = new DailyTaskStatusDTO();
            dto.setTaskId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setRemindTime(task.getRemindTime());
            dto.setRepeatType(task.getRepeatType());

            CheckInLog log = logMap.get(task.getId());
            dto.setStatus(calculateTaskStatus(task, log, isToday, nowTime, date));
            dto.setCheckTime(log != null ? log.getCheckTime() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean createTask(TaskDTO taskDTO) {
        CheckInTask task = new CheckInTask();
        BeanUtils.copyProperties(taskDTO, task);
        task.setStatus(TaskEnabled.ENABLED.getCode());
        return this.save(task);
    }

    /**
     * 判断任务在指定日期是否生效
     */
    private boolean isTaskActiveOnDate(CheckInTask task, LocalDate date, int dayOfWeek) {
        RepeatType repeatType = RepeatType.fromCode(task.getRepeatType());

        if (repeatType == RepeatType.ONCE) {
            LocalDateTime remindTime = task.getRemindTime();
            if (remindTime == null) {
                return false;
            }
            return remindTime.toLocalDate().equals(date);
        } else {
            return repeatType.matchesDayOfWeek(dayOfWeek);
        }
    }

    /**
     * 计算任务完成状态
     */
    private Integer calculateTaskStatus(CheckInTask task, CheckInLog log,
            boolean isToday, LocalTime nowTime, LocalDate date) {
        if (log != null) {
            return TaskStatus.COMPLETED.getCode();
        }

        if (isToday) {
            LocalDateTime remindTime = task.getRemindTime();
            if (remindTime != null && remindTime.toLocalTime().isBefore(nowTime)) {
                return TaskStatus.MISSED.getCode();
            }
            return TaskStatus.PENDING.getCode();
        } else if (date.isBefore(LocalDate.now())) {
            return TaskStatus.MISSED.getCode();
        } else {
            return TaskStatus.PENDING.getCode();
        }
    }
}
