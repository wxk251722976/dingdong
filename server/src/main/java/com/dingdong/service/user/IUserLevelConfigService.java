package com.dingdong.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.entity.user.UserLevelConfig;

/**
 * 用户等级配置服务接口
 */
public interface IUserLevelConfigService extends IService<UserLevelConfig> {

    /**
     * 根据等级编码获取配置
     *
     * @param levelCode 等级编码
     * @return 等级配置
     */
    UserLevelConfig getByLevelCode(String levelCode);

    /**
     * 获取用户的等级配置
     *
     * @param userId 用户ID
     * @return 等级配置
     */
    UserLevelConfig getUserLevelConfig(Long userId);
}
