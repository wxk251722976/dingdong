package com.dingdong.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.UserConstants;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserLevelConfig;
import com.dingdong.mapper.user.UserLevelConfigMapper;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.user.IUserLevelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 用户等级配置服务实现
 */
@Service
@RequiredArgsConstructor
public class UserLevelConfigServiceImpl extends ServiceImpl<UserLevelConfigMapper, UserLevelConfig>
        implements IUserLevelConfigService {

    @Lazy
    private final ISysUserService sysUserService;

    @Override
    public UserLevelConfig getByLevelCode(String levelCode) {
        return this.getOne(
                new LambdaQueryWrapper<UserLevelConfig>()
                        .eq(UserLevelConfig::getLevelCode, levelCode));
    }

    @Override
    public UserLevelConfig getUserLevelConfig(Long userId) {
        // 获取用户信息
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return getByLevelCode(UserConstants.DEFAULT_LEVEL_CODE);
        }

        String levelCode = user.getLevelCode();
        if (levelCode == null || levelCode.isEmpty()) {
            levelCode = UserConstants.DEFAULT_LEVEL_CODE;
        }

        UserLevelConfig config = getByLevelCode(levelCode);
        if (config == null) {
            // 如果找不到配置，返回默认配置
            config = new UserLevelConfig();
            config.setLevelCode(UserConstants.DEFAULT_LEVEL_CODE);
            config.setLevelName(UserConstants.DEFAULT_LEVEL_NAME);
            config.setMaxSupervisedCount(UserConstants.DEFAULT_MAX_SUPERVISED_COUNT);
            config.setMaxTaskPerUser(UserConstants.DEFAULT_MAX_TASK_PER_USER);
            config.setMaxFeedbackPerDay(UserConstants.DEFAULT_MAX_FEEDBACK_PER_DAY);
        }

        return config;
    }
}
