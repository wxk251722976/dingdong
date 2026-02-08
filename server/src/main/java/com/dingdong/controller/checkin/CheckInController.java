package com.dingdong.controller.checkin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dingdong.common.ErrorCode;
import com.dingdong.common.Result;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.checkin.CheckInDTO;
import com.dingdong.dto.checkin.SupervisedUserStatusDTO;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.user.IUserRelationService;
import com.dingdong.entity.checkin.CheckInTask;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

import com.dingdong.service.checkin.ICheckInTaskService;
import com.dingdong.vo.checkin.CheckInLogVO;
import com.dingdong.vo.checkin.SupervisedUserStatusVO;
import com.dingdong.vo.user.UserVO;

/**
 * 叮咚记录控制器
 * 处理叮咚打卡、查询记录及用户关系绑定
 */
@RestController
@RequestMapping("/checkIn")
@RequiredArgsConstructor
public class CheckInController {

    private final ICheckInLogService checkInLogService;
    private final IUserRelationService userRelationService;
    private final ICheckInTaskService checkInTaskService;

    /**
     * 获取监督用户的今日状态列表
     */
    @GetMapping("/supervisor/status")
    public Result<List<SupervisedUserStatusVO>> getSupervisedUserStatusList() {
        Long supervisorId = SystemContextHolder.getUserId();
        List<SupervisedUserStatusDTO> dtos = checkInTaskService.getSupervisedUserStatusList(supervisorId);

        List<SupervisedUserStatusVO> vos = dtos.stream().map(dto -> {
            SupervisedUserStatusVO vo = new SupervisedUserStatusVO();
            vo.setUserId(dto.getUserId());
            vo.setNickname(dto.getNickname());
            vo.setAvatar(dto.getAvatar());
            vo.setRelationName(dto.getRelationName());
            vo.setTodayStatus(dto.getTodayStatus());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(vos);
    }

    /**
     * 执行叮咚打卡操作
     * 
     * @param checkInDTO 包含 taskId (任务ID)
     * @return 操作结果
     */
    @PostMapping("/do")
    public Result<Boolean> doCheckIn(@RequestBody @Valid CheckInDTO checkInDTO) {
        // 从上下文获取用户ID
        checkInDTO.setUserId(SystemContextHolder.getUserId());
        boolean success = checkInLogService.doCheckIn(checkInDTO);
        return success ? Result.success(true) : Result.error(ErrorCode.CHECKIN_ALREADY_DONE);
    }

    /**
     * 查询当前用户叮咚记录 (分页)
     * 
     * @param pageNum  页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 叮咚记录分页列表
     */
    @GetMapping("/logs")
    public Result<IPage<CheckInLogVO>> getLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SystemContextHolder.getUserId();
        IPage<CheckInLog> limit = new Page<>(pageNum, pageSize);
        IPage<CheckInLog> pageResult = checkInLogService.getLogsByUserId(limit, userId);

        // Fetch task titles
        List<Long> taskIds = pageResult.getRecords().stream()
                .map(CheckInLog::getTaskId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> taskTitleMap;
        if (!taskIds.isEmpty()) {
            List<CheckInTask> tasks = checkInTaskService.listByIds(taskIds);
            taskTitleMap = tasks.stream()
                    .collect(Collectors.toMap(CheckInTask::getId, CheckInTask::getTitle));
        } else {
            taskTitleMap = Collections.emptyMap();
        }

        // 转换为 VO
        IPage<CheckInLogVO> result = pageResult.convert(log -> {
            CheckInLogVO vo = new CheckInLogVO();
            vo.setId(log.getId());
            vo.setTaskId(log.getTaskId());
            vo.setCheckTime(log.getCheckTime());
            vo.setStatus(log.getStatus());
            vo.setTaskTitle(taskTitleMap.getOrDefault(log.getTaskId(), "未知任务"));
            return vo;
        });

        return Result.success(result);
    }

    /**
     * 查询监督者绑定的被监督者列表
     * 
     * @return 绑定的被监督者用户列表
     */
    @GetMapping("/supervised")
    public Result<List<UserVO>> getSupervisedList() {
        Long supervisorId = SystemContextHolder.getUserId();
        List<SysUser> users = userRelationService.getSupervisedListBySupervisorId(supervisorId);

        List<UserVO> userVOs = users.stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setLevelCode(user.getLevelCode());
            vo.setLevelName(user.getLevelName());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(userVOs);
    }

    /**
     * 绑定被监督者与监督者关系
     * 
     * @param bindDTO 包含 supervisedId (被监督者ID)
     * @return 绑定成功返回 true
     */
    @PostMapping("/bind")
    public Result<Boolean> bindSupervised(@RequestBody BindDTO bindDTO) {
        try {
            // 从上下文获取监督者ID
            bindDTO.setSupervisorId(SystemContextHolder.getUserId());
            boolean success = userRelationService.bindSupervised(bindDTO);
            return success ? Result.success(true) : Result.error("绑定失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
