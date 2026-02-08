package com.dingdong.controller.checkin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dingdong.common.Result;
import com.dingdong.common.constant.TaskEnabled;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.checkin.DailyTaskStatusDTO;
import com.dingdong.dto.checkin.TaskDTO;
import com.dingdong.dto.checkin.TaskUpdateDTO;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.service.checkin.ICheckInTaskService;
import com.dingdong.service.user.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dingdong.entity.user.SysUser;
import com.dingdong.vo.checkin.DailyTaskStatusVO;
import com.dingdong.vo.checkin.TaskDetailVO;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final ICheckInTaskService taskService;
    private final ISysUserService sysUserService;

    /**
     * 创建新任务
     */
    @PostMapping("/add")
    public Result<Boolean> addTask(@RequestBody TaskDTO taskDTO) {
        // 从上下文获取创建者ID
        taskDTO.setCreatorId(SystemContextHolder.getUserId());
        return Result.success(taskService.createTask(taskDTO));
    }

    /**
     * 获取当前用户每日任务及完成状态
     */
    @GetMapping("/daily")
    public Result<List<DailyTaskStatusVO>> getDailyTasks(
            @RequestParam(required = false) String date) {
        Long userId = SystemContextHolder.getUserId();
        LocalDate localDate = (date == null) ? LocalDate.now() : LocalDate.parse(date);
        List<DailyTaskStatusDTO> dtos = taskService.getDailyTaskStatus(userId, localDate);

        List<DailyTaskStatusVO> vos = dtos.stream().map(dto -> {
            DailyTaskStatusVO vo = new DailyTaskStatusVO();
            vo.setTaskId(dto.getTaskId());
            vo.setTitle(dto.getTitle());
            vo.setRemindTime(dto.getRemindTime());
            vo.setRepeatType(dto.getRepeatType());
            vo.setStatus(dto.getStatus());
            vo.setCheckTime(dto.getCheckTime());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(vos);
    }

    /**
     * 获取指定被监督用户的任务列表（当前用户作为创建者/监督者）
     */
    @GetMapping("/list")
    public Result<List<DailyTaskStatusVO>> getTaskList(@RequestParam Long userId) {
        // creatorId从上下文获取（当前登录用户就是监督者）
        Long creatorId = SystemContextHolder.getUserId();
        List<DailyTaskStatusDTO> dtos = taskService.getDailyTaskStatusByCreator(userId, LocalDate.now(), creatorId);

        List<DailyTaskStatusVO> vos = dtos.stream().map(dto -> {
            DailyTaskStatusVO vo = new DailyTaskStatusVO();
            vo.setTaskId(dto.getTaskId());
            vo.setTitle(dto.getTitle());
            vo.setRemindTime(dto.getRemindTime());
            vo.setRepeatType(dto.getRepeatType());
            vo.setStatus(dto.getStatus());
            vo.setCheckTime(dto.getCheckTime());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(vos);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/detail")
    public Result<TaskDetailVO> getTaskDetail(@RequestParam Long taskId) {
        CheckInTask task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }

        TaskDetailVO vo = new TaskDetailVO();
        vo.setTaskId(task.getId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setRemindTime(task.getRemindTime());
        vo.setRepeatType(task.getRepeatType());
        vo.setUserId(task.getUserId());
        vo.setCreateTime(task.getCreateTime());

        // 获取任务目标用户名称
        if (task.getUserId() != null) {
            SysUser user = sysUserService.getById(task.getUserId());
            vo.setUserName(user != null ? user.getNickname() : "未知用户");
        }

        // 获取创建者信息
        if (task.getCreatorId() != null) {
            SysUser creator = sysUserService.getById(task.getCreatorId());
            if (creator != null) {
                vo.setCreatorId(creator.getId());
                vo.setCreatorName(creator.getNickname());
                vo.setCreatorAvatar(creator.getAvatar());
            }
        }

        return Result.success(vo);
    }

    /**
     * 更新任务
     */
    @PostMapping("/update")
    public Result<Boolean> updateTask(@RequestBody TaskUpdateDTO updateDTO) {
        CheckInTask task = taskService.getById(updateDTO.getTaskId());
        if (task == null) {
            return Result.error("任务不存在");
        }

        return Result.success(taskService.updateById(BeanUtil.toBean(updateDTO, CheckInTask.class)));
    }

    /**
     * 删除任务（软删除，设置为停用）
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteTask(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());

        boolean success = taskService.update(
                new LambdaUpdateWrapper<CheckInTask>()
                        .eq(CheckInTask::getId, taskId)
                        .set(CheckInTask::getStatus, TaskEnabled.DISABLED.getCode()));

        return Result.success(success);
    }
}
