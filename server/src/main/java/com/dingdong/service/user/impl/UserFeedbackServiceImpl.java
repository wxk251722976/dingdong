package com.dingdong.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.UserConstants;
import com.dingdong.entity.user.UserFeedback;
import com.dingdong.entity.user.UserLevelConfig;
import com.dingdong.mapper.user.UserFeedbackMapper;
import com.dingdong.service.user.IUserFeedbackService;
import com.dingdong.service.user.IUserLevelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户反馈服务实现
 */
@Service
@RequiredArgsConstructor
public class UserFeedbackServiceImpl extends ServiceImpl<UserFeedbackMapper, UserFeedback>
        implements IUserFeedbackService {

    private final IUserLevelConfigService userLevelConfigService;

    @Override
    public boolean submitFeedback(Long userId, String content, String contact) {
        // 检查今日反馈数量
        int todayCount = getTodayFeedbackCount(userId);
        UserLevelConfig config = userLevelConfigService.getUserLevelConfig(userId);
        int maxCount = config != null ? config.getMaxFeedbackPerDay() : UserConstants.DEFAULT_MAX_FEEDBACK_PER_DAY;

        if (todayCount >= maxCount) {
            throw new RuntimeException("今日反馈次数已达上限（" + maxCount + "次）");
        }

        // 保存反馈
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setContent(content);
        feedback.setContact(contact);
        feedback.setStatus(UserConstants.FEEDBACK_STATUS_PENDING);
        feedback.setCreateTime(LocalDateTime.now());

        return this.save(feedback);
    }

    @Override
    public int getTodayFeedbackCount(Long userId) {
        LocalDate today = LocalDate.now();
        return (int) this.count(
                new LambdaQueryWrapper<UserFeedback>()
                        .eq(UserFeedback::getUserId, userId)
                        .ge(UserFeedback::getCreateTime, today.atStartOfDay())
                        .lt(UserFeedback::getCreateTime, today.plusDays(1).atStartOfDay()));
    }
}
