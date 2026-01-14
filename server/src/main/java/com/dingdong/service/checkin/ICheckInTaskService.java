package com.dingdong.service.checkin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.entity.checkin.CheckInTask;

import java.time.LocalDate;
import java.util.List;

/**
 * CheckInTask Service Interface
 */
public interface ICheckInTaskService extends IService<CheckInTask> {
    /**
     * Get tasks for a user on a specific date (filtering by repeat rules)
     * 
     * @param userId target user ID
     * @param date   target date
     * @return List of tasks
     */
    List<CheckInTask> getTasksForUserDate(Long userId, LocalDate date);
}
