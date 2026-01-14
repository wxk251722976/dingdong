package com.dingdong.service.checkin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.dto.checkin.CheckInDTO;
import com.dingdong.entity.checkin.CheckInLog;

import java.util.List;

/**
 * 打卡日志业务接口
 * 
 * @author Antigravity
 */
public interface ICheckInLogService extends IService<CheckInLog> {

    /**
     * 执行打卡
     * 
     * @param checkInDTO 打卡参数
     * @return 成功返回 true
     */
    boolean doCheckIn(CheckInDTO checkInDTO);

    /**
     * 获取用户打卡日志
     * 
     * @param userId 用户 ID
     * @return 日志列表
     */
    List<CheckInLog> getLogsByUserId(Long userId);
}
