package com.dingdong.service.user;

import com.dingdong.dto.user.UserStatsDTO;

/**
 * 用户统计服务接口
 */
public interface IUserStatsService {

    /**
     * 获取用户统计数据
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    UserStatsDTO getUserStats(Long userId);
}
