package com.dingdong.service.checkin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.mapper.checkin.CheckInTaskMapper;
import com.dingdong.service.checkin.ICheckInTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CheckInTask Service Implementation
 */
@Service
public class CheckInTaskServiceImpl extends ServiceImpl<CheckInTaskMapper, CheckInTask> implements ICheckInTaskService {

    @Override
    public List<CheckInTask> getTasksForUserDate(Long userId, LocalDate date) {
        // 1. Get all enabled tasks for the user
        LambdaQueryWrapper<CheckInTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInTask::getUserId, userId)
                .eq(CheckInTask::getStatus, 1);
        List<CheckInTask> allTasks = this.list(wrapper);

        // 2. Filter by repeatDays
        // repeatDays format: "1,2,3,4,5" (Monday=1, Sunday=7)
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)

        return allTasks.stream().filter(task -> {
            String repeatRules = task.getRepeatDays();
            if (repeatRules == null || repeatRules.isEmpty()) {
                // If empty, assume one-time task? Or daily? Let's assume daily if not specified
                // or handle one-time differently.
                // Requirement says "Options include Daily, Workdays, Weekends".
                // Let's assume "1,2,3,4,5,6,7" for daily.
                return false;
            }
            return repeatRules.contains(String.valueOf(dayOfWeek));
        }).collect(Collectors.toList());
    }
}
