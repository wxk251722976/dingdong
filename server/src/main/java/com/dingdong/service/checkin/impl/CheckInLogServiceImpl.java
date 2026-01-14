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
public class CheckInLogServiceImpl extends ServiceImpl<CheckInLogMapper, CheckInLog> implements ICheckInLogService {

    @Override
    public boolean doCheckIn(CheckInDTO checkInDTO) {
        CheckInLog log = new CheckInLog();
        log.setUserId(checkInDTO.getUserId());
        log.setTaskId(checkInDTO.getTaskId());
        log.setCheckTime(LocalDateTime.now());
        log.setStatus(CheckInStatus.NORMAL.getCode());
        log.setCreateTime(LocalDateTime.now());
        return this.save(log);
    }

    @Override
    public List<CheckInLog> getLogsByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<CheckInLog>()
                .eq(CheckInLog::getUserId, userId)
                .orderByDesc(CheckInLog::getCheckTime));
    }
}
