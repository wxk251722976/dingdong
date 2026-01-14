package com.dingdong.controller.checkin;

import com.dingdong.common.Result;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.dto.checkin.CheckInDTO;
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

    /**
     * 执行叮咚打卡操作
     * 
     * @param checkInDTO 包含 userId (用户ID) 和 taskId (任务ID)
     * @return 操作成功返回 true
     */
    @PostMapping("/do")
    public Result<Boolean> doCheckIn(@RequestBody CheckInDTO checkInDTO) {
        boolean success = checkInLogService.doCheckIn(checkInDTO);
        return success ? Result.success(true) : Result.error("叮咚失败");
    }

    /**
     * 查询用户叮咚记录
     * 
     * @param userId 用户ID
     * @return 叮咚记录列表，按时间倒序排列
     */
    @GetMapping("/logs")
    public Result<List<CheckInLog>> getLogs(@RequestParam Long userId) {
        return Result.success(checkInLogService.getLogsByUserId(userId));
    }

    /**
     * 查询子女绑定的老人列表
     * 
     * @param childId 子女ID
     * @return 绑定的老人用户列表
     */
    @GetMapping("/elders")
    public Result<List<SysUser>> getMyElders(@RequestParam Long childId) {
        return Result.success(userRelationService.getEldersByChildId(childId));
    }

    /**
     * 绑定老人与子女关系
     * 
     * @param bindDTO 包含 childId (子女ID) 和 elderId (老人ID)
     * @return 绑定成功返回 true
     */
    @PostMapping("/bind")
    public Result<Boolean> bindElder(@RequestBody BindDTO bindDTO) {
        try {
            boolean success = userRelationService.bindElder(bindDTO);
            return success ? Result.success(true) : Result.error("绑定失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
