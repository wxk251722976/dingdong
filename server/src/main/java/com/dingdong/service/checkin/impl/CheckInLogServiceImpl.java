package com.dingdong.service.checkin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.CheckInStatus;
import com.dingdong.common.constant.RepeatType;
import com.dingdong.common.exception.ServiceException;
import com.dingdong.dto.checkin.CheckInDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.checkin.CheckInTask;
import com.dingdong.entity.user.SysUser;
import com.dingdong.mapper.checkin.CheckInLogMapper;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.wechat.SubscribeMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 打卡日志业务实现
 * 
 * @author Antigravity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInLogServiceImpl extends ServiceImpl<CheckInLogMapper, CheckInLog> implements ICheckInLogService {

    private final com.dingdong.mapper.checkin.CheckInTaskMapper checkInTaskMapper;
    private final ISysUserService sysUserService;
    private final SubscribeMessageService subscribeMessageService;

    @Override
    public boolean doCheckIn(CheckInDTO checkInDTO) {
        // 1. 获取并验证任务
        CheckInTask task = getAndValidateTask(checkInDTO.getTaskId());

        // 2. 计算并验证打卡时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = calculateTargetTime(task);
        validateCheckInTime(now, targetTime);

        // 3. 构建并保存打卡记录
        CheckInLog checkInLog = createCheckInLog(checkInDTO, now, targetTime);
        boolean saved = this.save(checkInLog);

        // 4. 发送通知
        if (saved && task.getCreatorId() != null) {
            boolean isOnTime = !now.isAfter(targetTime);
            sendCheckInNotificationAsync(task, checkInDTO.getUserId(), now, isOnTime);
        }

        return saved;
    }

    /**
     * 获取并验证任务是否存在
     */
    private CheckInTask getAndValidateTask(Long taskId) {
        CheckInTask task = checkInTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        return task;
    }

    /**
     * 计算当次打卡的目标时间
     */
    private LocalDateTime calculateTargetTime(CheckInTask task) {
        if (task.getRemindTime() == null) {
            throw new ServiceException("任务时间未设置");
        }

        if (RepeatType.ONCE.getCode().equals(task.getRepeatType())) {
            return task.getRemindTime();
        } else {
            // 对于重复任务，取当天的提醒时间
            return LocalDateTime.of(LocalDate.now(), task.getRemindTime().toLocalTime());
        }
    }

    /**
     * 验证打卡时间是否有效（在目标时间后30分钟内）
     */
    private void validateCheckInTime(LocalDateTime now, LocalDateTime targetTime) {
        int limitMinutes = 30;
        LocalDateTime limitTime = targetTime.plusMinutes(limitMinutes);

        if (now.isAfter(limitTime)) {
            throw new ServiceException("已超过打卡时限(" + limitMinutes + "分钟)，无法打卡");
        }
    }

    /**
     * 构建打卡日志实体
     */
    private CheckInLog createCheckInLog(CheckInDTO checkInDTO, LocalDateTime now, LocalDateTime targetTime) {
        CheckInLog checkInLog = new CheckInLog();
        checkInLog.setUserId(checkInDTO.getUserId());
        checkInLog.setTaskId(checkInDTO.getTaskId());
        checkInLog.setCheckTime(now);
        checkInLog.setCreateTime(now);

        // 判断打卡状态 (正常 vs 补打卡)
        boolean isOnTime = !now.isAfter(targetTime);
        checkInLog.setStatus(isOnTime ? CheckInStatus.NORMAL.getCode() : CheckInStatus.REISSUE.getCode());

        return checkInLog;
    }

    /**
     * 异步发送打卡完成通知（目前同步调用，预留异步扩展点）
     */
    private void sendCheckInNotificationAsync(CheckInTask task, Long userId, LocalDateTime checkTime,
            boolean isOnTime) {
        try {
            // 获取监督者信息
            SysUser supervisor = sysUserService.getById(task.getCreatorId());
            if (supervisor == null || supervisor.getOpenid() == null) {
                log.warn("监督者不存在或无openid，无法发送打卡完成通知: supervisorId={}", task.getCreatorId());
                return;
            }

            // 获取被监督者信息
            SysUser supervised = sysUserService.getById(userId);
            String supervisedName = supervised != null ? supervised.getNickname() : "未知用户";

            log.info("发送打卡完成通知给监督者: supervisorId={}, supervisedName={}, taskTitle={}",
                    task.getCreatorId(), supervisedName, task.getTitle());

            subscribeMessageService.sendCheckInCompleteMessage(
                    supervisor.getOpenid(),
                    supervisedName,
                    task.getTitle(),
                    checkTime,
                    isOnTime);
        } catch (Exception e) {
            // 通知失败不影响打卡结果
            log.error("发送打卡完成通知失败", e);
        }
    }

    @Override
    public List<CheckInLog> getLogsByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<CheckInLog>()
                .eq(CheckInLog::getUserId, userId)
                .orderByDesc(CheckInLog::getCheckTime));
    }
}
