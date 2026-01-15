package com.dingdong.controller.checkin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dingdong.common.Result;
import com.dingdong.common.constant.TaskEnabled;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.checkin.DailyTaskStatusDTO;
import com.dingdong.dto.checkin.TaskDTO;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.service.checkin.ICheckInTaskService;
import com.dingdong.service.user.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Result<List<DailyTaskStatusDTO>> getDailyTasks(
            @RequestParam(required = false) String date) {
        Long userId = SystemContextHolder.getUserId();
        LocalDate localDate = (date == null) ? LocalDate.now() : LocalDate.parse(date);
        return Result.success(taskService.getDailyTaskStatus(userId, localDate));
    }

    /**
     * 获取指定被监督用户的任务列表（当前用户作为创建者/监督者）
     */
    @GetMapping("/list")
    public Result<List<DailyTaskStatusDTO>> getTaskList(@RequestParam Long userId) {
        // creatorId从上下文获取（当前登录用户就是监督者）
        Long creatorId = SystemContextHolder.getUserId();
        return Result.success(taskService.getDailyTaskStatusByCreator(userId, LocalDate.now(), creatorId));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/detail")
    public Result<Map<String, Object>> getTaskDetail(@RequestParam Long taskId) {
        CheckInTask task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId());
        result.put("title", task.getTitle());
        result.put("remindTime", task.getRemindTime());
        result.put("repeatType", task.getRepeatType());
        result.put("userId", task.getUserId());
        result.put("createTime", task.getCreateTime());

        // 获取任务目标用户名称
        if (task.getUserId() != null) {
            com.dingdong.entity.user.SysUser user = sysUserService.getById(task.getUserId());
            result.put("userName", user != null ? user.getNickname() : "未知用户");
        }

        // 获取创建者信息
        if (task.getCreatorId() != null) {
            com.dingdong.entity.user.SysUser creator = sysUserService.getById(task.getCreatorId());
            if (creator != null) {
                result.put("creatorId", creator.getId());
                result.put("creatorName", creator.getNickname());
                result.put("creatorAvatar", creator.getAvatar());
            }
        }

        return Result.success(result);
    }

    /**
     * 更新任务
     */
    @PostMapping("/update")
    public Result<Boolean> updateTask(@RequestBody Map<String, Object> params) {
        Long taskId = Long.valueOf(params.get("taskId").toString());

        CheckInTask task = taskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }

        if (params.containsKey("title")) {
            task.setTitle((String) params.get("title"));
        }
        if (params.containsKey("repeatType")) {
            task.setRepeatType(Integer.valueOf(params.get("repeatType").toString()));
        }
        if (params.containsKey("remindTime")) {
            task.setRemindTime(LocalDateTime.parse((String) params.get("remindTime")));
        }

        return Result.success(taskService.updateById(task));
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
