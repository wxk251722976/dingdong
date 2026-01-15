package com.dingdong.service.checkin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.CheckInStatus;
import com.dingdong.dto.checkin.CheckInDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.mapper.checkin.CheckInLogMapper;
import com.dingdong.service.checkin.ICheckInLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 打卡日志业务实现
 * 
 * @author Antigravity
 */
@Service
@lombok.RequiredArgsConstructor
public class CheckInLogServiceImpl extends ServiceImpl<CheckInLogMapper, CheckInLog> implements ICheckInLogService {

    private final com.dingdong.mapper.checkin.CheckInTaskMapper checkInTaskMapper;

    @Override
    public boolean doCheckIn(CheckInDTO checkInDTO) {
        // 1. 获取任务信息
        com.dingdong.entity.checkin.CheckInTask task = checkInTaskMapper.selectById(checkInDTO.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime remindTime = task.getRemindTime();
        LocalDateTime targetTime;

        // 2. 计算目标打卡时间
        if (com.dingdong.common.constant.RepeatType.ONCE.getCode().equals(task.getRepeatType())) {
            targetTime = remindTime;
        } else {
            // 对于重复任务，取当天的提醒时间
            targetTime = LocalDateTime.of(java.time.LocalDate.now(), remindTime.toLocalTime());
        }

        if (targetTime == null) {
            throw new RuntimeException("任务时间未设置");
        }

        // 3. 判断打卡时效
        LocalDateTime limitTime = targetTime.plusMinutes(30);

        if (now.isAfter(limitTime)) {
            throw new RuntimeException("已超过打卡时限(30分钟)，无法打卡");
        }

        CheckInLog log = new CheckInLog();
        log.setUserId(checkInDTO.getUserId());
        log.setTaskId(checkInDTO.getTaskId());
        log.setCheckTime(now);
        log.setCreateTime(now);

        // 4. 判断打卡状态 (正常 vs 补打卡)
        if (now.isAfter(targetTime)) {
            log.setStatus(CheckInStatus.REISSUE.getCode());
        } else {
            log.setStatus(CheckInStatus.NORMAL.getCode());
        }

        return this.save(log);
    }

    @Override
    public List<CheckInLog> getLogsByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<CheckInLog>()
                .eq(CheckInLog::getUserId, userId)
                .orderByDesc(CheckInLog::getCheckTime));
    }
}
