package com.dingdong.controller.checkin;

import com.dingdong.common.Result;
import com.dingdong.dto.checkin.DailyTaskStatusDTO;
import com.dingdong.dto.checkin.TaskDTO;
import com.dingdong.service.checkin.ICheckInTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final ICheckInTaskService taskService;

    /**
     * 创建新任务
     */
    @PostMapping("/add")
    public Result<Boolean> addTask(@RequestBody TaskDTO taskDTO) {
        return Result.success(taskService.createTask(taskDTO));
    }

    /**
     * 获取用户每日任务及完成状态
     */
    @GetMapping("/daily")
    public Result<List<DailyTaskStatusDTO>> getDailyTasks(
            @RequestParam Long userId,
            @RequestParam(required = false) String date) {
        LocalDate localDate = (date == null) ? LocalDate.now() : LocalDate.parse(date);
        return Result.success(taskService.getDailyTaskStatus(userId, localDate));
    }
}
