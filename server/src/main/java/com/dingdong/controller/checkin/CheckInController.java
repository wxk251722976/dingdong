package com.dingdong.controller.checkin;

import com.dingdong.common.Result;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.checkin.CheckInDTO;
import com.dingdong.dto.checkin.SupervisedUserStatusDTO;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.user.IUserRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final com.dingdong.service.checkin.ICheckInTaskService checkInTaskService;

    /**
     * 获取监督用户的今日状态列表
     */
    @GetMapping("/supervisor/status")
    public Result<List<SupervisedUserStatusDTO>> getSupervisedUserStatusList() {
        Long supervisorId = SystemContextHolder.getUserId();
        return Result.success(checkInTaskService.getSupervisedUserStatusList(supervisorId));
    }

    /**
     * 执行叮咚打卡操作
     * 
     * @param checkInDTO 包含 taskId (任务ID)
     * @return 操作成功返回 true
     */
    @PostMapping("/do")
    public Result<Boolean> doCheckIn(@RequestBody CheckInDTO checkInDTO) {
        // 从上下文获取用户ID
        checkInDTO.setUserId(SystemContextHolder.getUserId());
        boolean success = checkInLogService.doCheckIn(checkInDTO);
        return success ? Result.success(true) : Result.error("叮咚失败");
    }

    /**
     * 查询当前用户叮咚记录
     * 
     * @return 叮咚记录列表，按时间倒序排列
     */
    @GetMapping("/logs")
    public Result<List<CheckInLog>> getLogs() {
        Long userId = SystemContextHolder.getUserId();
        return Result.success(checkInLogService.getLogsByUserId(userId));
    }

    /**
     * 查询监督者绑定的被监督者列表
     * 
     * @return 绑定的被监督者用户列表
     */
    @GetMapping("/supervised")
    public Result<List<SysUser>> getSupervisedList() {
        Long supervisorId = SystemContextHolder.getUserId();
        return Result.success(userRelationService.getSupervisedListBySupervisorId(supervisorId));
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
