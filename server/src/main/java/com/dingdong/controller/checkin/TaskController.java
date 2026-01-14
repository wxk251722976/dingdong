package com.dingdong.controller.checkin;

import com.dingdong.common.Result;
import com.dingdong.dto.checkin.DailyTaskStatusDTO;
import com.dingdong.dto.checkin.TaskDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.checkin.ICheckInTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ICheckInTaskService taskService;
    @Autowired
    private ICheckInLogService logService;

    @PostMapping("/add")
    public Result<Boolean> addTask(@RequestBody TaskDTO taskDTO) {
        CheckInTask task = new CheckInTask();
        BeanUtils.copyProperties(taskDTO, task);
        task.setStatus(1); // Enable by default
        return Result.success(taskService.save(task));
    }

    /**
     * Get tasks for a specific user and date.
     * Useful for building the daily task list.
     */
    @GetMapping("/daily")
    public Result<List<DailyTaskStatusDTO>> getDailyTasks(@RequestParam Long userId,
            @RequestParam(required = false) String date) {
        LocalDate localDate;
        if (date == null) {
            localDate = LocalDate.now();
        } else {
            localDate = LocalDate.parse(date);
        }

        List<CheckInTask> tasks = taskService.getTasksForUserDate(userId, localDate);

        // Fetch logs for the day
        LambdaQueryWrapper<CheckInLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(CheckInLog::getUserId, userId)
                .ge(CheckInLog::getCheckTime, localDate.atStartOfDay())
                .lt(CheckInLog::getCheckTime, localDate.plusDays(1).atStartOfDay());

        List<CheckInLog> logs = logService.list(logWrapper);

        Map<Long, CheckInLog> logMap = logs.stream()
                .filter(l -> l.getTaskId() != null)
                .collect(Collectors.toMap(CheckInLog::getTaskId, Function.identity(), (a, b) -> a));

        List<DailyTaskStatusDTO> result = new ArrayList<>();
        LocalTime nowTime = LocalTime.now();
        boolean isToday = localDate.equals(LocalDate.now());

        for (CheckInTask task : tasks) {
            DailyTaskStatusDTO dto = new DailyTaskStatusDTO();
            dto.setTask(task);
            CheckInLog log = logMap.get(task.getId());
            dto.setLog(log);

            if (log != null) {
                dto.setStatus("COMPLETED");
            } else {
                if (isToday && task.getRemindTime() != null && task.getRemindTime().isBefore(nowTime)) {
                    // Check if strictly missed or just pending.
                    // For UI simplicity: if remind time passed and not done -> PENDING (but maybe
                    // bold?).
                    // User requirement "Missed" usually implies can't do it anymore?
                    // Or just late? Let's stick to PENDING vs MISSED logic.
                    // The prompt mock showed "Missed" for 10:00 when it was 15:00.
                    dto.setStatus("MISSED");
                } else if (localDate.isBefore(LocalDate.now())) {
                    dto.setStatus("MISSED");
                } else {
                    dto.setStatus("PENDING");
                }
            }
            result.add(dto);
        }
        return Result.success(result);
    }
}
