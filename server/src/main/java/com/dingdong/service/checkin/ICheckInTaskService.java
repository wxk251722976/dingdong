package com.dingdong.service.checkin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.dto.checkin.DailyTaskStatusDTO;
import com.dingdong.dto.checkin.SupervisedUserStatusDTO;
import com.dingdong.dto.checkin.TaskDTO;
import com.dingdong.entity.checkin.CheckInTask;

import java.time.LocalDate;
import java.util.List;

/**
 * 叮咚任务服务接口
 */
public interface ICheckInTaskService extends IService<CheckInTask> {

    /**
     * 获取用户每日任务及完成状态
     * 
     * @param userId 用户ID
     * @param date   日期
     * @return 任务状态列表
     */
    List<DailyTaskStatusDTO> getDailyTaskStatus(Long userId, LocalDate date);

    /**
     * 创建新任务
     */
    boolean createTask(TaskDTO taskDTO);

    /**
     * 获取用户每日任务及完成状态（限定创建者）
     *
     * @param userId    用户ID
     * @param date      日期
     * @param creatorId 创建者ID
     * @return 任务状态列表
     */
    List<DailyTaskStatusDTO> getDailyTaskStatusByCreator(Long userId, LocalDate date, Long creatorId);

    /**
     * 获取监督用户的今日状态列表
     *
     * @param supervisorId 监督者ID
     * @return 用户状态列表
     */
    List<SupervisedUserStatusDTO> getSupervisedUserStatusList(Long supervisorId);
}
